package com.sds.bookrecommendation.recommendation;

import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.repository.BookRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Recommends the best-rated books across a list of genres, for example the genres
 * a reader got from the personality quiz. Genres are taken in turn, so every genre
 * gets a fair share of the list. Box sets and collections are skipped.
 */
@Service
public class GenreRecommendationService {

    static final int DEFAULT_LIMIT = 12;
    static final int MAX_LIMIT = 50;
    static final int MAX_GENRES = 6;

    /** A book needs at least this many stored (dataset) ratings to be recommended. */
    static final int MIN_STORED_RATINGS = TopRatedStrategy.MIN_DATASET_RATINGS;

    /** Titles that are bundles rather than single books. */
    private static final Pattern BUNDLE =
            Pattern.compile("box(ed)? ?set|collection|omnibus|treasury|study bible", Pattern.CASE_INSENSITIVE);

    private final BookRepository bookRepository;

    public GenreRecommendationService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * Returns up to {@code limit} well-rated books from the given genres.
     *
     * @param genres genre names; case and spaces are ignored, at most 6 are used
     * @param limit  number of books wanted; null means 12, values above 50 become 50
     * @throws IllegalArgumentException if no genre is given or the limit is below 1
     */
    @Transactional(readOnly = true)
    public List<RecommendedBook> byGenres(List<String> genres, Integer limit) {
        int count = resolveLimit(limit);
        List<String> wanted = normalise(genres);
        if (wanted.isEmpty()) {
            throw new IllegalArgumentException("genres must contain at least one genre");
        }

        // Best books of each genre, bundles removed.
        List<List<Book>> perGenre = new ArrayList<>();
        for (String genre : wanted) {
            List<Book> ranked = bookRepository
                    .findByGenreIgnoreCaseAndRatingCountGreaterThanEqualOrderByAvgRatingDescRatingCountDesc(
                            genre, MIN_STORED_RATINGS, PageRequest.of(0, count * 3))
                    .stream()
                    .filter(book -> !BUNDLE.matcher(book.getTitle()).find())
                    .toList();
            perGenre.add(ranked);
        }

        // Take one book from each genre in turn until the list is full.
        List<RecommendedBook> result = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (int round = 0; result.size() < count; round++) {
            boolean tookAny = false;
            for (int i = 0; i < wanted.size() && result.size() < count; i++) {
                List<Book> ranked = perGenre.get(i);
                if (round < ranked.size()) {
                    tookAny = true;
                    Book book = ranked.get(round);
                    if (seen.add(book.getId())) {
                        result.add(toRecommendation(book, wanted.get(i)));
                    }
                }
            }
            if (!tookAny) {
                break;
            }
        }
        return result;
    }

    private static RecommendedBook toRecommendation(Book book, String genre) {
        double average = Math.round(book.getAvgRating() * 100) / 100.0;
        String reason = String.format(Locale.ROOT,
                "Matches your quiz result: a top-rated %s book (%.1f out of 5 by %,d Goodreads readers)",
                genre, book.getAvgRating(), book.getRatingCount());
        return new RecommendedBook(book.getId(), book.getTitle(), book.getAuthor(),
                book.getGenre(), average, reason);
    }

    /** Trims, lower-cases, removes blanks and duplicates, and keeps at most MAX_GENRES. */
    private static List<String> normalise(List<String> genres) {
        Set<String> clean = new LinkedHashSet<>();
        if (genres != null) {
            for (String genre : genres) {
                if (genre != null && !genre.isBlank()) {
                    clean.add(genre.trim().toLowerCase(Locale.ROOT));
                }
            }
        }
        return clean.stream().limit(MAX_GENRES).toList();
    }

    private static int resolveLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be at least 1");
        }
        return Math.min(limit, MAX_LIMIT);
    }
}