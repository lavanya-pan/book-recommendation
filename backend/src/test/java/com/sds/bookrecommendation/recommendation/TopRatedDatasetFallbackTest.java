package com.sds.bookrecommendation.recommendation;

import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.Rating;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Top rated falls back to the rating stored on each book when the site has no ratings yet. */
class TopRatedDatasetFallbackTest {

    private RatingRepository ratingRepository;
    private BookRepository bookRepository;
    private TopRatedStrategy strategy;
    private Book first;
    private Book second;

    @BeforeEach
    void setUp() {
        ratingRepository = mock(RatingRepository.class);
        bookRepository = mock(BookRepository.class);
        strategy = new TopRatedStrategy(ratingRepository, bookRepository);

        first = storedBook(1L, "First", 4.8, 5000);
        second = storedBook(2L, "Second", 4.5, 9000);
        when(ratingRepository.findBookAverages()).thenReturn(List.of());
        when(ratingRepository.findByUserId(7L)).thenReturn(List.of());
        when(bookRepository.findByRatingCountGreaterThanEqualOrderByAvgRatingDescRatingCountDesc(
                eq(TopRatedStrategy.MIN_DATASET_RATINGS), any(Pageable.class)))
                .thenReturn(List.of(first, second));
    }

    @Test
    void usesStoredRatingsWhenTheSiteHasNoRatings() {
        List<RecommendedBook> result = strategy.recommend(7L, 5);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals(4.8, result.get(0).score());
        assertTrue(result.get(0).reason().contains("Goodreads"));
    }

    @Test
    void skipsBooksTheUserAlreadyRated() {
        User user = new User("Reader", "reader@example.com");
        user.setId(7L);
        when(ratingRepository.findByUserId(7L)).thenReturn(List.of(new Rating(user, first, 5)));

        List<RecommendedBook> result = strategy.recommend(7L, 5);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).id());
    }

    @Test
    void respectsTheLimit() {
        List<RecommendedBook> result = strategy.recommend(7L, 1);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
    }

    private static Book storedBook(long id, String title, double average, int count) {
        Book book = new Book(title, "Author", "fantasy", "A book", 2000);
        book.setId(id);
        book.setAvgRating(average);
        book.setRatingCount(count);
        return book;
    }
}