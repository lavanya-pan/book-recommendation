package com.sds.bookrecommendation.recommendation;

import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.Rating;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.RatingRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Recommends well-rated books from the genres a user likes most.
 * A genre counts as "liked" when the user rated a book in it LIKED_SCORE or higher.
 * Score = (share of the user's liked books in that genre) x (book's average rating).
 */
@Component
public class GenreStrategy implements RecommendationStrategy {

    /** A rating at or above this value means the user liked the book. */
    static final int LIKED_SCORE = 4;

    /** A candidate book needs at least this many ratings to be recommended. */
    static final int MIN_RATINGS = 2;

    /** Only the user's top genres are used. */
    static final int MAX_GENRES = 3;

    private final RatingRepository ratingRepository;
    private final BookRepository bookRepository;

    public GenreStrategy(RatingRepository ratingRepository, BookRepository bookRepository) {
        this.ratingRepository = ratingRepository;
        this.bookRepository = bookRepository;
    }

    /** Name used in the request parameter. */
    @Override
    public String type() {
        return "genre";
    }

    /**
     * Returns up to {@code limit} books from the user's favourite genres.
     * Returns an empty list when the user has not liked any book yet,
     * so the service can fall back to another strategy (cold start).
     */
    @Override
    @Transactional(readOnly = true)
    public List<RecommendedBook> recommend(Long userId, int limit) {
        if (userId == null || limit <= 0) {
            return List.of();
        }

        // Step 1: find which books the user rated, and count liked books per genre.
        Set<Long> alreadyRated = new HashSet<>();
        Map<String, Integer> likedPerGenre = new HashMap<>();
        for (Rating rating : ratingRepository.findByUserId(userId)) {
            Book book = rating.getBook();
            alreadyRated.add(book.getId());
            if (rating.getScore() >= LIKED_SCORE && book.getGenre() != null) {
                likedPerGenre.merge(book.getGenre().toLowerCase(), 1, Integer::sum);
            }
        }
        if (likedPerGenre.isEmpty()) {
            return List.of();
        }

        // Step 2: keep the user's top genres and how strongly each one is liked.
        List<Map.Entry<String, Integer>> topGenres = likedPerGenre.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(MAX_GENRES)
                .toList();
        int totalLiked = topGenres.stream().mapToInt(Map.Entry::getValue).sum();

        // Step 3: average rating and rating count of every rated book: [average, count].
        Map<Long, double[]> stats = new HashMap<>();
        for (Object[] row : ratingRepository.findBookAverages()) {
            stats.put(((Number) row[0]).longValue(),
                    new double[]{((Number) row[1]).doubleValue(), ((Number) row[2]).doubleValue()});
        }

        // Step 4: score the unrated books of each favourite genre.
        List<RecommendedBook> result = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (Map.Entry<String, Integer> genre : topGenres) {
            double genreShare = (double) genre.getValue() / totalLiked;
            for (Book book : bookRepository.findByGenreIgnoreCase(genre.getKey())) {
                double[] bookStats = stats.get(book.getId());
                if (alreadyRated.contains(book.getId()) || bookStats == null
                        || bookStats[1] < MIN_RATINGS || !seen.add(book.getId())) {
                    continue;
                }
                double score = Math.round(genreShare * bookStats[0] * 100) / 100.0;
                result.add(new RecommendedBook(book.getId(), book.getTitle(), book.getAuthor(),
                        book.getGenre(), score, "Because you enjoy " + genre.getKey() + " books"));
            }
        }

        // Step 5: best score first, then cut to the limit.
        result.sort(Comparator.comparingDouble(RecommendedBook::score).reversed());
        return result.stream().limit(limit).toList();
    }
}