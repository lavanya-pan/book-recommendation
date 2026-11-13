package com.sds.bookrecommendation.recommendation;

import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.Rating;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.RatingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Recommendations that do not depend on a user: the best-rated books overall
 * and the books that received the most ratings recently.
 */
@Service
public class PopularBooksService {

    static final int MIN_RATINGS = 3;
    static final int TRENDING_DAYS = 30;
    static final int DEFAULT_LIMIT = 10;
    static final int MAX_LIMIT = 50;

    private final RatingRepository ratingRepository;
    private final BookRepository bookRepository;

    public PopularBooksService(RatingRepository ratingRepository, BookRepository bookRepository) {
        this.ratingRepository = ratingRepository;
        this.bookRepository = bookRepository;
    }

    /**
     * Highest average rating first, among books with at least 3 ratings.
     *
     * @param limit number of books wanted; null means 10, values above 50 become 50
     * @throws IllegalArgumentException if the limit is below 1
     */
    @Transactional(readOnly = true)
    public List<RecommendedBook> topRated(Integer limit) {
        int count = resolveLimit(limit);
        List<Long> ids = new ArrayList<>();
        Map<Long, Double> scores = new HashMap<>();
        Map<Long, String> reasons = new HashMap<>();

        // Each row is [bookId, averageScore, ratingCount], best average first.
        for (Object[] row : ratingRepository.findBookAverages()) {
            long ratings = ((Number) row[2]).longValue();
            if (ratings < MIN_RATINGS) {
                continue;
            }
            Long id = ((Number) row[0]).longValue();
            double average = ((Number) row[1]).doubleValue();
            ids.add(id);
            scores.put(id, Math.round(average * 100) / 100.0);
            reasons.put(id, String.format(Locale.ROOT, "Rated %.1f out of 5 by %d readers", average, ratings));
            if (ids.size() == count) {
                break;
            }
        }
        return build(ids, scores, reasons);
    }

    /**
     * Most ratings in the last 30 days first; ties go to the higher average rating.
     *
     * @param limit number of books wanted; null means 10, values above 50 become 50
     * @throws IllegalArgumentException if the limit is below 1
     */
    @Transactional(readOnly = true)
    public List<RecommendedBook> trending(Integer limit) {
        int count = resolveLimit(limit);
        LocalDateTime since = LocalDateTime.now().minusDays(TRENDING_DAYS);

        Map<Long, Integer> recentCounts = new HashMap<>();
        Map<Long, Integer> recentSums = new HashMap<>();
        for (Rating rating : ratingRepository.findAll()) {
            if (rating.getRatedAt() == null || rating.getRatedAt().isBefore(since)) {
                continue;
            }
            long bookId = rating.getBook().getId();
            recentCounts.merge(bookId, 1, Integer::sum);
            recentSums.merge(bookId, rating.getScore(), Integer::sum);
        }

        List<Long> ids = recentCounts.keySet().stream()
                .sorted((a, b) -> {
                    int byCount = Integer.compare(recentCounts.get(b), recentCounts.get(a));
                    if (byCount != 0) {
                        return byCount;
                    }
                    int byAverage = Double.compare(average(recentSums, recentCounts, b),
                            average(recentSums, recentCounts, a));
                    return byAverage != 0 ? byAverage : Long.compare(a, b);
                })
                .limit(count)
                .toList();

        Map<Long, Double> scores = new HashMap<>();
        Map<Long, String> reasons = new HashMap<>();
        for (Long id : ids) {
            int ratings = recentCounts.get(id);
            scores.put(id, (double) ratings);
            reasons.put(id, ratings + (ratings == 1 ? " rating" : " ratings")
                    + " in the last " + TRENDING_DAYS + " days");
        }
        return build(ids, scores, reasons);
    }

    private static double average(Map<Long, Integer> sums, Map<Long, Integer> counts, Long id) {
        return (double) sums.get(id) / counts.get(id);
    }

    /** Loads the books and builds the response objects, keeping the given order. */
    private List<RecommendedBook> build(List<Long> ids, Map<Long, Double> scores,
                                        Map<Long, String> reasons) {
        Map<Long, Book> books = bookRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));

        List<RecommendedBook> result = new ArrayList<>();
        for (Long id : ids) {
            Book book = books.get(id);
            if (book != null) {
                result.add(new RecommendedBook(book.getId(), book.getTitle(), book.getAuthor(),
                        book.getGenre(), scores.get(id), reasons.get(id)));
            }
        }
        return result;
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