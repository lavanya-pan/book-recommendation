package com.sds.bookrecommendation.recommendation;

import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.Rating;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for TopRatedStrategy using fake repositories (no database needed).
 */
@ExtendWith(MockitoExtension.class)
class TopRatedStrategyTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private BookRepository bookRepository;

    private TopRatedStrategy strategy;

    /** Builds the strategy with the fake repositories before each test. */
    @BeforeEach
    void setUp() {
        strategy = new TopRatedStrategy(ratingRepository, bookRepository);
    }

    /** Creates a simple book with the given id and title. */
    private Book book(long id, String title) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setAuthor("Author " + id);
        book.setGenre("Fantasy");
        return book;
    }

    /** Fake database rows: [bookId, average, count], best average first. */
    private void givenAverages() {
        when(ratingRepository.findBookAverages()).thenReturn(List.of(
                new Object[]{2L, 5.0, 1L},   // perfect score but only 1 rating
                new Object[]{1L, 4.8, 10L},
                new Object[]{3L, 4.5, 5L}));
        when(bookRepository.findAllById(any())).thenReturn(
                List.of(book(1, "Book One"), book(2, "Book Two"), book(3, "Book Three")));
    }

    @Test
    void ranksByAverageAndIgnoresBooksWithTooFewRatings() {
        givenAverages();

        List<RecommendedBook> result = strategy.recommend(null, 10);

        assertEquals(List.of(1L, 3L), result.stream().map(RecommendedBook::id).toList());
        assertEquals(4.8, result.get(0).score(), 0.001);
    }

    @Test
    void doesNotRecommendBooksTheUserAlreadyRated() {
        givenAverages();
        when(ratingRepository.findByUserId(7L))
                .thenReturn(List.of(new Rating(null, book(1, "Book One"), 5)));

        List<RecommendedBook> result = strategy.recommend(7L, 10);

        assertEquals(List.of(3L), result.stream().map(RecommendedBook::id).toList());
    }

    @Test
    void respectsTheLimit() {
        givenAverages();

        List<RecommendedBook> result = strategy.recommend(null, 1);

        assertEquals(List.of(1L), result.stream().map(RecommendedBook::id).toList());
    }

    @Test
    void returnsNothingWhenLimitIsZero() {
        assertTrue(strategy.recommend(null, 0).isEmpty());
    }
}