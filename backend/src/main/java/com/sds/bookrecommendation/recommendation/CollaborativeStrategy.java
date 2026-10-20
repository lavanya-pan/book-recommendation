package com.sds.bookrecommendation.recommendation;

import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.Rating;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.RatingRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Item-based collaborative filtering: "readers who liked this also liked...".
 * Two books are similar when the same readers rated them (cosine similarity of
 * their rating vectors). Only ratings are used, never the book text.
 */
@Component
public class CollaborativeStrategy implements RecommendationStrategy {

    /** A rating at or above this value means the user liked the book. */
    static final int LIKED_SCORE = 4;

    /** A book pair needs at least this many readers who rated both. */
    static final int MIN_CO_RATERS = 2;

    private final RatingRepository ratingRepository;
    private final BookRepository bookRepository;

    public CollaborativeStrategy(RatingRepository ratingRepository, BookRepository bookRepository) {
        this.ratingRepository = ratingRepository;
        this.bookRepository = bookRepository;
    }

    /** Name used in the request parameter. */
    @Override
    public String type() {
        return "collaborative";
    }

    /**
     * Returns up to {@code limit} unrated books that readers with similar taste liked.
     * Returns an empty list when the user has not liked any book yet or when there is
     * not enough rating data, so the service can fall back to another strategy.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RecommendedBook> recommend(Long userId, int limit) {
        if (userId == null || limit <= 0) {
            return List.of();
        }

        // Step 1: put every rating into two lookup tables (by user and by book).
        Map<Long, Map<Long, Integer>> byUser = new HashMap<>();
        Map<Long, Map<Long, Integer>> byBook = new HashMap<>();
        for (Rating rating : ratingRepository.findAll()) {
            long user = rating.getUser().getId();
            long book = rating.getBook().getId();
            byUser.computeIfAbsent(user, k -> new HashMap<>()).put(book, rating.getScore());
            byBook.computeIfAbsent(book, k -> new HashMap<>()).put(user, rating.getScore());
        }

        Map<Long, Integer> mine = byUser.get(userId);
        if (mine == null) {
            return List.of();
        }
        Map<Long, Integer> liked = new HashMap<>();
        mine.forEach((book, score) -> {
            if (score >= LIKED_SCORE) {
                liked.put(book, score);
            }
        });
        if (liked.isEmpty()) {
            return List.of();
        }

        // Step 2: length of each book's rating vector, used by the cosine formula.
        Map<Long, Double> norms = new HashMap<>();
        byBook.forEach((book, raters) -> {
            double sum = 0;
            for (int score : raters.values()) {
                sum += (double) score * score;
            }
            norms.put(book, Math.sqrt(sum));
        });

        // Step 3: for each liked book, score the books other readers rated alongside it.
        Map<Long, Double> scores = new HashMap<>();
        Map<Long, Long> bestSeed = new HashMap<>();
        Map<Long, Double> bestContribution = new HashMap<>();

        for (Map.Entry<Long, Integer> seed : liked.entrySet()) {
            long seedId = seed.getKey();
            Map<Long, Double> dots = new HashMap<>();
            Map<Long, Integer> coRaters = new HashMap<>();

            for (Map.Entry<Long, Integer> rater : byBook.get(seedId).entrySet()) {
                int seedScore = rater.getValue();
                for (Map.Entry<Long, Integer> other : byUser.get(rater.getKey()).entrySet()) {
                    long otherId = other.getKey();
                    if (otherId == seedId || mine.containsKey(otherId)) {
                        continue;
                    }
                    dots.merge(otherId, (double) seedScore * other.getValue(), Double::sum);
                    coRaters.merge(otherId, 1, Integer::sum);
                }
            }

            for (Map.Entry<Long, Double> entry : dots.entrySet()) {
                long candidate = entry.getKey();
                if (coRaters.get(candidate) < MIN_CO_RATERS) {
                    continue;
                }
                double similarity = entry.getValue() / (norms.get(seedId) * norms.get(candidate));
                double contribution = similarity * seed.getValue();
                scores.merge(candidate, contribution, Double::sum);
                if (contribution > bestContribution.getOrDefault(candidate, 0.0)) {
                    bestContribution.put(candidate, contribution);
                    bestSeed.put(candidate, seedId);
                }
            }
        }

        // Step 4: best scores first (ties broken by book id so results are stable).
        List<Long> ranked = scores.entrySet().stream()
                .sorted((a, b) -> {
                    int byScore = Double.compare(b.getValue(), a.getValue());
                    return byScore != 0 ? byScore : Long.compare(a.getKey(), b.getKey());
                })
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();

        return toRecommendations(ranked, scores, bestSeed);
    }

    /** Loads the books and builds the response objects, keeping the ranked order. */
    private List<RecommendedBook> toRecommendations(List<Long> ranked, Map<Long, Double> scores,
                                                    Map<Long, Long> bestSeed) {
        Set<Long> needed = new HashSet<>(ranked);
        for (Long id : ranked) {
            needed.add(bestSeed.get(id));
        }
        Map<Long, Book> books = bookRepository.findAllById(needed).stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));

        List<RecommendedBook> result = new ArrayList<>();
        for (Long id : ranked) {
            Book book = books.get(id);
            if (book == null) {
                continue;
            }
            Book seedBook = books.get(bestSeed.get(id));
            String seedTitle = seedBook == null ? "a book you liked" : seedBook.getTitle();
            double score = Math.round(scores.get(id) * 1000) / 1000.0;
            result.add(new RecommendedBook(book.getId(), book.getTitle(), book.getAuthor(),
                    book.getGenre(), score,
                    "Readers who liked \"" + seedTitle + "\" also liked this"));
        }
        return result;
    }
}