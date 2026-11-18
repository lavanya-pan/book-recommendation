package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.dto.MoodQuizRequest;
import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.quiz.MoodPreferences;
import com.sds.bookrecommendation.recommendation.RecommendedBook;
import com.sds.bookrecommendation.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns mood quiz answers into preferences and finds matching books.
 */
@Service
public class MoodQuizService {

    /** Books up to this many pages count as short. */
    static final int SHORT_MAX_PAGES = 250;

    /** Books from this many pages count as long. */
    static final int LONG_MIN_PAGES = 500;

    /** The most books one request can return. */
    static final int MAX_LIMIT = 50;

    private static final Map<String, List<String>> MOOD_GENRES = Map.of(
            "cozy", List.of("contemporary", "romance", "humor"),
            "thrilling", List.of("thriller", "mystery", "adventure"),
            "emotional", List.of("romance", "contemporary", "poetry"),
            "funny", List.of("humor", "contemporary"),
            "dark", List.of("horror", "crime", "thriller"));

    private static final Map<String, List<String>> MOOD_KEYWORDS = Map.of(
            "cozy", List.of("cozy", "heartwarming", "friendship"),
            "thrilling", List.of("suspense", "action", "thriller"),
            "emotional", List.of("emotional", "love", "loss"),
            "funny", List.of("funny", "comedy", "humor"),
            "dark", List.of("dark", "murder", "haunting"));

    private static final Map<String, List<String>> WORLD_GENRES = Map.of(
            "fantasy", List.of("fantasy"),
            "real-world", List.of("contemporary", "biography", "non-fiction"),
            "scifi", List.of("sci-fi"),
            "historical", List.of("history", "classics"));

    private static final Map<String, List<String>> TONE_KEYWORDS = Map.of(
            "light", List.of("light", "fun"),
            "serious", List.of("serious", "thoughtful"),
            "hopeful", List.of("hope", "hopeful", "uplifting"),
            "mysterious", List.of("mystery", "secret", "hidden"));

    private final BookRepository bookRepository;

    public MoodQuizService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * Converts the quiz answers into preferences. Throws IllegalArgumentException (400)
     * when a value is not one of the allowed answers.
     */
    public MoodPreferences moodToPreferences(MoodQuizRequest request) {
        String mood = normalize(request.mood());
        String world = normalize(request.world());
        String tone = normalize(request.tone());
        boolean romance = Boolean.TRUE.equals(request.romance());

        Map<String, Double> weights = new LinkedHashMap<>();
        lookup(MOOD_GENRES, mood, "mood").forEach(g -> weights.merge(g, 1.0, Double::sum));
        lookup(WORLD_GENRES, world, "world").forEach(g -> weights.merge(g, 1.0, Double::sum));
        if (romance) {
            weights.merge("romance", 1.0, Double::sum);
        }

        List<String> keywords = new ArrayList<>(lookup(MOOD_KEYWORDS, mood, "mood"));
        keywords.addAll(lookup(TONE_KEYWORDS, tone, "tone"));

        int[] range = pageRange(normalize(request.length()));
        return new MoodPreferences(weights, List.copyOf(keywords), range[0], range[1], romance);
    }

    /** Returns up to {@code limit} books that fit the quiz answers, best match first. */
    @Transactional(readOnly = true)
    public List<RecommendedBook> recommend(MoodQuizRequest request, int limit) {
        MoodPreferences prefs = moodToPreferences(request);
        int safeLimit = Math.max(1, Math.min(limit, MAX_LIMIT));

        Map<Long, Book> candidates = new LinkedHashMap<>();
        for (String genre : prefs.genreWeights().keySet()) {
            bookRepository.findByGenreIgnoreCase(genre)
                    .forEach(book -> candidates.putIfAbsent(book.getId(), book));
        }

        String mood = normalize(request.mood());
        return candidates.values().stream()
                .filter(book -> fitsLength(book, prefs))
                .filter(book -> prefs.romance() || !"romance".equalsIgnoreCase(book.getGenre()))
                .map(book -> toRecommendation(book, prefs, mood))
                .sorted(Comparator.comparingDouble(RecommendedBook::score).reversed())
                .limit(safeLimit)
                .toList();
    }

    /** True when the book's page count is inside the range, or unknown. */
    private boolean fitsLength(Book book, MoodPreferences prefs) {
        Integer pages = book.getPageCount();
        return pages == null || (pages >= prefs.minPages() && pages <= prefs.maxPages());
    }

    /** Scores one book: genre match, keyword match and average rating. */
    private RecommendedBook toRecommendation(Book book, MoodPreferences prefs, String mood) {
        double maxWeight = Collections.max(prefs.genreWeights().values());
        String genre = book.getGenre() == null ? "" : book.getGenre().toLowerCase();
        double genreScore = prefs.genreWeights().getOrDefault(genre, 0.0) / maxWeight;

        String text = ((book.getTitle() == null ? "" : book.getTitle()) + " "
                + (book.getTags() == null ? "" : book.getTags()) + " "
                + (book.getDescription() == null ? "" : book.getDescription())).toLowerCase();
        long hits = prefs.keywords().stream().filter(text::contains).count();
        double keywordScore = prefs.keywords().isEmpty() ? 0 : (double) hits / prefs.keywords().size();

        double score = 0.6 * genreScore + 0.2 * keywordScore + 0.2 * (book.getAvgRating() / 5.0);
        return new RecommendedBook(book.getId(), book.getTitle(), book.getAuthor(), book.getGenre(),
                Math.round(score * 100) / 100.0, "Matches your " + mood + " mood");
    }

    /** Finds the page range for short, medium or long. */
    private int[] pageRange(String length) {
        return switch (length) {
            case "short" -> new int[]{0, SHORT_MAX_PAGES};
            case "medium" -> new int[]{SHORT_MAX_PAGES, LONG_MIN_PAGES};
            case "long" -> new int[]{LONG_MIN_PAGES, Integer.MAX_VALUE};
            default -> throw new IllegalArgumentException(
                    "Unknown length '" + length + "'. Valid values: short, medium, long");
        };
    }

    /** Looks up an answer in a table, or throws a 400 that lists the valid answers. */
    private static List<String> lookup(Map<String, List<String>> table, String key, String field) {
        List<String> value = table.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Unknown " + field + " '" + key
                    + "'. Valid values: " + table.keySet().stream().sorted().toList());
        }
        return value;
    }

    /** Lowercases and trims an answer; null becomes an empty text. */
    private static String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase();
    }
}