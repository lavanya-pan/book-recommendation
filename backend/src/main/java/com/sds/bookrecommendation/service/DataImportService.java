package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.dto.ImportSummary;
import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Loads the Goodbooks-10k catalogue (books.csv, tags.csv, book_tags.csv) into the books table.
 * Every book gets one of the team's official genres. Safe to run more than once: books that
 * already exist (same title and author) are skipped.
 */
@Service
public class DataImportService {

    private static final Logger log = LoggerFactory.getLogger(DataImportService.class);

    static final String BOOKS_FILE = "data/books.csv";
    static final String TAGS_FILE = "data/tags.csv";
    static final String BOOK_TAGS_FILE = "data/book_tags.csv";

    /** Books are saved in groups of this size. */
    static final int BATCH_SIZE = 500;

    /** How many of a book's most popular tags are stored in its tags column. */
    static final int MAX_TAGS_PER_BOOK = 8;

    /**
     * When true, the dataset's average rating and rating count are the starting values,
     * so the top-rated row works before any user rates anything. The rating module
     * overwrites them when it recomputes a book's average.
     */
    static final boolean USE_DATASET_RATINGS = true;

    /** The team's official genre list. Names must match exactly across the project. */
    static final List<String> OFFICIAL_GENRES = List.of(
            "fantasy", "adventure", "sci-fi", "mystery", "crime", "thriller", "romance",
            "history", "non-fiction", "philosophy", "science", "biography", "classics",
            "contemporary", "humor", "young-adult", "poetry", "horror");

    /** Genre given to a book whose tags match no official genre. */
    static final String FALLBACK_GENRE = "contemporary";

    /**
     * When true, books that match no official genre are skipped instead of getting the fallback.
     * Keep this false: skipping shifts the database ids, and the ratings import relies on
     * database id = dataset book_id.
     */
    static final boolean SKIP_UNMATCHED = false;

    /** Author used when the author is blank (the author column cannot be empty). */
    static final String UNKNOWN_AUTHOR = "Unknown author";

    /** Maps a dataset tag to the official genre it counts for. */
    static final Map<String, String> GENRE_ALIASES = buildGenreAliases();

    /** Shelf names that say nothing about the book's content. */
    private static final Set<String> JUNK_TAGS = Set.copyOf(Arrays.asList(
            "to-read", "currently-reading", "favorites", "favourites", "favorite", "owned",
            "books-i-own", "my-books", "default", "library", "kindle", "ebook", "ebooks",
            "e-book", "audiobook", "audiobooks", "audio", "i-own", "owned-books", "to-buy",
            "wish-list", "wishlist", "read", "have", "own", "re-read", "abandoned", "dnf",
            "did-not-finish", "unfinished", "books", "my-library"));

    /** Tags that contain a year, such as "read-in-2015" or "2016-reads", are shelves too. */
    private static final Pattern YEAR_TAG = Pattern.compile(".*\\d{4}.*");

    /** One tag with the number of users who gave it to a book. */
    record TagCount(String name, int count) {
    }

    /** One row of a CSV file, read by column name. */
    private static final class CsvRow {
        private final Map<String, Integer> index;
        private final List<String> values;

        /** Creates a row from the header positions and the row's values. */
        CsvRow(Map<String, Integer> index, List<String> values) {
            this.index = index;
            this.values = values;
        }

        /** Returns the trimmed value of a column, or an empty string when it is missing. */
        String get(String column) {
            Integer position = index.get(column);
            if (position == null || position >= values.size()) {
                return "";
            }
            return values.get(position).trim();
        }
    }

    private final BookRepository bookRepository;

    /** Receives the repository through the constructor. */
    public DataImportService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * Reads the three CSV files and saves every new book, skipping blank titles and duplicates.
     * Returns how many rows were read, saved and skipped.
     */
    public ImportSummary importBooks() {
        Map<Long, String> tagNames = loadTagNames();
        Map<Long, List<TagCount>> tagsByBook = loadBookTags(tagNames);

        Set<String> seen = new HashSet<>();
        for (Book existing : bookRepository.findAll()) {
            seen.add(duplicateKey(existing.getTitle(), existing.getAuthor()));
        }

        AtomicInteger total = new AtomicInteger();
        AtomicInteger blank = new AtomicInteger();
        AtomicInteger duplicate = new AtomicInteger();
        AtomicInteger noGenre = new AtomicInteger();
        AtomicInteger fallback = new AtomicInteger();
        AtomicInteger imported = new AtomicInteger();
        List<Book> batch = new ArrayList<>();

        forEachRow(BOOKS_FILE, List.of("goodreads_book_id", "title", "authors"), row -> {
            total.incrementAndGet();

            String title = truncate(row.get("title"), 255);
            if (title.isBlank()) {
                blank.incrementAndGet();
                return;
            }
            String author = truncate(
                    row.get("authors").isBlank() ? UNKNOWN_AUTHOR : row.get("authors"), 255);
            if (!seen.add(duplicateKey(title, author))) {
                duplicate.incrementAndGet();
                return;
            }

            List<TagCount> tags = tagsByBook.getOrDefault(parseLong(row.get("goodreads_book_id")), List.of());
            String genre = pickGenre(tags);
            if (genre == null) {
                if (SKIP_UNMATCHED) {
                    noGenre.incrementAndGet();
                    return;
                }
                genre = FALLBACK_GENRE;
                fallback.incrementAndGet();
            }
            batch.add(buildBook(row, title, author, genre, tags));

            if (batch.size() >= BATCH_SIZE) {
                bookRepository.saveAll(batch);
                imported.addAndGet(batch.size());
                batch.clear();
                log.info("Imported {} books so far", imported.get());
            }
        });

        if (!batch.isEmpty()) {
            bookRepository.saveAll(batch);
            imported.addAndGet(batch.size());
        }
        log.info("Book import finished: {} rows, {} saved, {} blank titles, {} duplicates, {} no genre, {} fallback genre",
                total.get(), imported.get(), blank.get(), duplicate.get(), noGenre.get(), fallback.get());
        return new ImportSummary(total.get(), imported.get(), blank.get(), duplicate.get(),
                noGenre.get(), fallback.get());
    }

    /** Creates a Book from one CSV row, its chosen genre and its tags. */
    private Book buildBook(CsvRow row, String title, String author, String genre, List<TagCount> tags) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setGenre(genre);
        book.setTags(truncate(topTags(tags), 1000));
        book.setPublishedYear(parseYear(row.get("original_publication_year")));
        book.setCoverUrl(cleanCoverUrl(row.get("image_url")));
        if (USE_DATASET_RATINGS) {
            book.setAvgRating(parseDouble(row.get("average_rating")));
            book.setRatingCount(parseInt(row.get("ratings_count")));
        }
        return book;
    }

    /** Builds the tag-to-official-genre table. Fails at startup if a target is not an official genre. */
    private static Map<String, String> buildGenreAliases() {
        Map<String, String> map = new HashMap<>();
        addAliases(map, "fantasy", "fantasy", "urban-fantasy", "epic-fantasy", "high-fantasy",
                "paranormal", "magic", "vampires", "fairy-tales");
        addAliases(map, "adventure", "adventure");
        addAliases(map, "sci-fi", "sci-fi", "science-fiction", "scifi", "dystopian", "dystopia",
                "post-apocalyptic");
        addAliases(map, "mystery", "mystery", "mysteries", "mystery-thriller", "cozy-mystery", "detective");
        addAliases(map, "crime", "crime", "true-crime");
        addAliases(map, "thriller", "thriller", "thrillers", "suspense");
        addAliases(map, "romance", "romance", "chick-lit", "contemporary-romance", "historical-romance",
                "paranormal-romance");
        addAliases(map, "history", "history", "historical", "historical-fiction", "world-war-ii");
        addAliases(map, "non-fiction", "non-fiction", "nonfiction", "self-help", "business", "politics",
                "economics", "travel", "cookbooks", "cooking", "health");
        addAliases(map, "philosophy", "philosophy", "religion", "spirituality", "theology");
        addAliases(map, "science", "science", "psychology", "nature", "mathematics", "popular-science");
        addAliases(map, "biography", "biography", "memoir", "memoirs", "autobiography", "biography-memoir");
        addAliases(map, "classics", "classics", "classic");
        addAliases(map, "contemporary", "contemporary", "contemporary-fiction", "literary-fiction",
                "realistic-fiction");
        addAliases(map, "humor", "humor", "humour", "comedy", "funny");
        addAliases(map, "young-adult", "young-adult", "ya", "teen");
        addAliases(map, "poetry", "poetry", "poems");
        addAliases(map, "horror", "horror", "gothic");
        return Map.copyOf(map);
    }

    /** Registers dataset tags that count for one official genre. */
    private static void addAliases(Map<String, String> map, String genre, String... tags) {
        if (!OFFICIAL_GENRES.contains(genre)) {
            throw new IllegalStateException("Not an official genre: " + genre);
        }
        for (String tag : tags) {
            map.put(tag, genre);
        }
    }

    /** Reads tags.csv into a map from tag ID to lower-case tag name. */
    private Map<Long, String> loadTagNames() {
        Map<Long, String> names = new HashMap<>();
        forEachRow(TAGS_FILE, List.of("tag_id", "tag_name"), row -> {
            Long id = parseLong(row.get("tag_id"));
            if (id != null) {
                names.put(id, row.get("tag_name").toLowerCase(Locale.ROOT));
            }
        });
        return names;
    }

    /**
     * Reads book_tags.csv into a map from goodreads_book_id to that book's useful tags.
     * The file links books through goodreads_book_id, not through book_id.
     */
    private Map<Long, List<TagCount>> loadBookTags(Map<Long, String> tagNames) {
        Map<Long, List<TagCount>> result = new HashMap<>();
        forEachRow(BOOK_TAGS_FILE, List.of("goodreads_book_id", "tag_id", "count"), row -> {
            Long bookId = parseLong(row.get("goodreads_book_id"));
            Long tagId = parseLong(row.get("tag_id"));
            if (bookId == null || tagId == null) {
                return;
            }
            String name = tagNames.get(tagId);
            if (name == null || isJunkTag(name)) {
                return;
            }
            result.computeIfAbsent(bookId, id -> new ArrayList<>())
                    .add(new TagCount(name, parseInt(row.get("count"))));
        });
        return result;
    }

    /** Returns true for shelf names that are not about the book's content. */
    private static boolean isJunkTag(String name) {
        return JUNK_TAGS.contains(name) || YEAR_TAG.matcher(name).matches();
    }

    /**
     * Picks the book's official genre: tags are mapped to official genres, their counts are added
     * up per genre, and the genre with the highest total wins. Returns null when no tag matches.
     */
    static String pickGenre(List<TagCount> tags) {
        Map<String, Integer> totals = new LinkedHashMap<>();
        for (TagCount tag : tags) {
            String genre = GENRE_ALIASES.get(tag.name());
            if (genre != null) {
                totals.merge(genre, tag.count(), Integer::sum);
            }
        }
        String best = null;
        int bestTotal = -1;
        for (Map.Entry<String, Integer> entry : totals.entrySet()) {
            if (entry.getValue() > bestTotal) {
                best = entry.getKey();
                bestTotal = entry.getValue();
            }
        }
        return best;
    }

    /** Returns the book's most popular tags as one comma-separated text. */
    private static String topTags(List<TagCount> tags) {
        return tags.stream()
                .sorted(Comparator.comparingInt(TagCount::count).reversed())
                .limit(MAX_TAGS_PER_BOOK)
                .map(TagCount::name)
                .collect(Collectors.joining(", "));
    }

    /** Returns the cover URL, or null when it is blank, too long or a Goodreads placeholder. */
    private static String cleanCoverUrl(String url) {
        if (url.isBlank() || url.contains("nophoto") || url.length() > 500) {
            return null;
        }
        return url;
    }

    /** Builds the key used to detect duplicates: lower-case title and author. */
    private static String duplicateKey(String title, String author) {
        return (title.trim() + "|" + author.trim()).toLowerCase(Locale.ROOT);
    }

    /** Cuts a text to at most max characters so it fits its database column. */
    private static String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }

    /** Parses a whole number, returning null when the text is not a number. */
    private static Long parseLong(String value) {
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Parses a whole number, returning 0 when the text is not a number. */
    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Parses a decimal number, returning 0 when the text is not a number. */
    private static double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /** Parses a year such as "2008.0", returning null when it is blank or invalid. */
    private static Integer parseYear(String value) {
        try {
            return (int) Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Reads a CSV file from the classpath one row at a time and passes each row to the consumer.
     * Fails with a readable message (shown as HTTP 400) when the file or a required column is missing.
     */
    private void forEachRow(String path, List<String> requiredColumns, Consumer<CsvRow> consumer) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException(path + " is empty");
            }
            if (headerLine.startsWith("\uFEFF")) {
                headerLine = headerLine.substring(1);
            }
            List<String> headers = parseLine(headerLine);
            Map<String, Integer> index = new HashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                index.put(headers.get(i).trim(), i);
            }
            for (String column : requiredColumns) {
                if (!index.containsKey(column)) {
                    throw new IllegalArgumentException(
                            path + " has no column '" + column + "'. Columns found: " + headers);
                }
            }

            String line;
            while ((line = reader.readLine()) != null) {
                while (countQuotes(line) % 2 != 0) {
                    String next = reader.readLine();
                    if (next == null) {
                        break;
                    }
                    line = line + "\n" + next;
                }
                if (!line.isBlank()) {
                    consumer.accept(new CsvRow(index, parseLine(line)));
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read " + path + ": " + e.getMessage());
        }
    }

    /** Counts the quote characters in a line, to detect a quoted value that continues on the next line. */
    private static int countQuotes(String line) {
        int count = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '"') {
                count++;
            }
        }
        return count;
    }

    /** Splits one CSV line into its values, handling quoted values that contain commas or doubled quotes. */
    static List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields;
    }
}