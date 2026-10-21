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
import static org.mockito.Mockito.when;

/**
 * Unit tests for GenreStrategy using fake repositories (no database needed).
 */
@ExtendWith(MockitoExtension.class)
class GenreStrategyTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private BookRepository bookRepository;

    private GenreStrategy strategy;

    /** Builds the strategy with the fake repositories before each test. */
    @BeforeEach
    void setUp() {
        strategy = new GenreStrategy(ratingRepository, bookRepository);
    }

    /** Creates a book with the given id and genre. */
    private Book book(long id, String genre) {
        Book book = new Book();
        book.setId(id);
        book.setTitle("Book " + id);
        book.setAuthor("Author " + id);
        book.setGenre(genre);
        return book;
    }

    /** Creates a rating of a book by an unnamed user. */
    private Rating rating(Book book, int score) {
        return new Rating(null, book, score);
    }

    /** One row of fake database statistics: [bookId, average, count]. */
    private Object[] stats(long bookId, double average, long count) {
        return new Object[]{bookId, average, count};
    }

    /**
     * User 7 loves fantasy: rated books 1 and 2 (fantasy) with 5 and 4, and book 3 (romance) with 2.
     * Candidate fantasy books: 4 (avg 4.0), 5 (avg 4.6), and 6 (avg 5.0 but only 1 rating).
     */
    private void givenFantasyFan() {
        when(ratingRepository.findByUserId(7L)).thenReturn(List.of(
                rating(book(1, "Fantasy"), 5),
                rating(book(2, "Fantasy"), 4),
                rating(book(3, "Romance"), 2)));
        when(ratingRepository.findBookAverages()).thenReturn(List.of(
                stats(5, 4.6, 3), stats(4, 4.0, 5), stats(6, 5.0, 1)));
        when(bookRepository.findByGenreIgnoreCase("fantasy")).thenReturn(List.of(
                book(1, "Fantasy"), book(2, "Fantasy"), book(4, "Fantasy"),
                book(5, "Fantasy"), book(6, "Fantasy")));
    }

    @Test
    void recommendsUnratedBooksFromLikedGenresBestFirst() {
        givenFantasyFan();

        List<RecommendedBook> result = strategy.recommend(7L, 10);

        // Books 1 and 2 are already rated, book 6 has too few ratings.
        assertEquals(List.of(5L, 4L), result.stream().map(RecommendedBook::id).toList());
        assertEquals(4.6, result.get(0).score(), 0.001);
    }

    @Test
    void respectsTheLimit() {
        givenFantasyFan();

        List<RecommendedBook> result = strategy.recommend(7L, 1);

        assertEquals(List.of(5L), result.stream().map(RecommendedBook::id).toList());
    }

    @Test
    void weightsGenresByHowMuchTheUserLikesThem() {
        // Two liked fantasy books and one liked romance book: fantasy share 2/3, romance 1/3.
        when(ratingRepository.findByUserId(8L)).thenReturn(List.of(
                rating(book(1, "Fantasy"), 5),
                rating(book(2, "Fantasy"), 5),
                rating(book(3, "Romance"), 4)));
        when(ratingRepository.findBookAverages()).thenReturn(List.of(
                stats(10, 4.5, 4), stats(20, 5.0, 4)));
        when(bookRepository.findByGenreIgnoreCase("fantasy")).thenReturn(List.of(book(10, "Fantasy")));
        when(bookRepository.findByGenreIgnoreCase("romance")).thenReturn(List.of(book(20, "Romance")));

        List<RecommendedBook> result = strategy.recommend(8L, 10);

        // Fantasy: 2/3 x 4.5 = 3.0. Romance: 1/3 x 5.0 = 1.67.
        assertEquals(List.of(10L, 20L), result.stream().map(RecommendedBook::id).toList());
        assertEquals(3.0, result.get(0).score(), 0.001);
        assertEquals(1.67, result.get(1).score(), 0.001);
    }

    @Test
    void returnsNothingWhenTheUserLikedNoBook() {
        when(ratingRepository.findByUserId(9L)).thenReturn(List.of(rating(book(1, "Fantasy"), 2)));

        assertTrue(strategy.recommend(9L, 10).isEmpty());
    }

    @Test
    void returnsNothingForAVisitor() {
        assertTrue(strategy.recommend(null, 10).isEmpty());
    }
}