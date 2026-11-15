package com.sds.bookrecommendation.recommendation;

import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.RatingRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Recommends the books with the highest average rating.
 * Books with fewer than MIN_RATINGS site ratings are ignored so that a single
 * 5-star vote cannot put a book on top. When the site has too few ratings to fill
 * the list, the remaining places are filled from the rating stored on each book
 * (the dataset's average and rating count). This also makes it the cold-start
 * fallback for users who have not rated anything yet.
 */
@Component
public class TopRatedStrategy implements RecommendationStrategy {

    /** A book needs at least this many ratings on the site to qualify. Raise it when more data exists. */
    static final int MIN_RATINGS = 3;

    /** A book needs at least this many stored (dataset) ratings to be used as a filler. */
    static final int MIN_DATASET_RATINGS = 1000;

    private final RatingRepository ratingRepository;
    private final BookRepository bookRepository;

    public TopRatedStrategy(RatingRepository ratingRepository, BookRepository bookRepository) {
        this.ratingRepository = ratingRepository;
        this.bookRepository = bookRepository;
    }

    /** Name used in the request parameter. */
    @Override
    public String type() {
        return "top-rated";
    }

    /**
     * Returns up to {@code limit} best-rated books the user has not rated yet.
     * A null userId (a visitor) skips the "already rated" filter.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RecommendedBook> recommend(Long userId, int limit) {
        if (limit <= 0) {
            return List.of();
        }

        // Books this user already rated are never recommended again.
        Set<Long> alreadyRated = new HashSet<>();
        if (userId != null) {
            ratingRepository.findByUserId(userId)
                    .forEach(r -> alreadyRated.add(r.getBook().getId()));
        }

        // Rows are [bookId, averageScore, ratingCount], best average first.
        List<Object[]> qualified = new ArrayList<>();
        for (Object[] row : ratingRepository.findBookAverages()) {
            long bookId = ((Number) row[0]).longValue();
            long count = ((Number) row[2]).longValue();
            if (count >= MIN_RATINGS && !alreadyRated.contains(bookId)) {
                qualified.add(row);
            }
            if (qualified.size() == limit) {
                break;
            }
        }

        // Load all needed books in one query, then build the result in ranked order.
        List<Long> ids = qualified.stream()
                .map(row -> ((Number) row[0]).longValue())
                .collect(Collectors.toList());
        Map<Long, Book> booksById = bookRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));

        List<RecommendedBook> result = new ArrayList<>();
        Set<Long> included = new HashSet<>();
        for (Object[] row : qualified) {
            Book book = booksById.get(((Number) row[0]).longValue());
            if (book == null) {
                continue;
            }
            double average = Math.round(((Number) row[1]).doubleValue() * 10) / 10.0;
            long count = ((Number) row[2]).longValue();
            included.add(book.getId());
            result.add(new RecommendedBook(book.getId(), book.getTitle(), book.getAuthor(),
                    book.getGenre(), average,
                    "Rated " + average + " out of 5 by " + count + " readers"));
        }

        // Fill the remaining places from the stored (dataset) ratings.
        if (result.size() < limit) {
            int fetch = limit + alreadyRated.size() + included.size();
            List<Book> stored = bookRepository
                    .findByRatingCountGreaterThanEqualOrderByAvgRatingDescRatingCountDesc(
                            MIN_DATASET_RATINGS, PageRequest.of(0, fetch));
            for (Book book : stored) {
                if (result.size() == limit) {
                    break;
                }
                if (alreadyRated.contains(book.getId()) || included.contains(book.getId())) {
                    continue;
                }
                double average = Math.round(book.getAvgRating() * 10) / 10.0;
                included.add(book.getId());
                result.add(new RecommendedBook(book.getId(), book.getTitle(), book.getAuthor(),
                        book.getGenre(), average,
                        String.format(Locale.ROOT, "Rated %.1f out of 5 by %,d Goodreads readers",
                                average, book.getRatingCount())));
            }
        }
        return result;
    }
}