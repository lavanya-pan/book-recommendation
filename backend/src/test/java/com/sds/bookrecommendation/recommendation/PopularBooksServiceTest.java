package com.sds.bookrecommendation.recommendation;

import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.Rating;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PopularBooksServiceTest {

    private RatingRepository ratingRepository;
    private BookRepository bookRepository;
    private PopularBooksService service;

    private Book book1;
    private Book book2;
    private Book book3;
    private Book book4;

    @BeforeEach
    void setUp() {
        ratingRepository = mock(RatingRepository.class);
        bookRepository = mock(BookRepository.class);
        service = new PopularBooksService(ratingRepository, bookRepository);

        book1 = book(1L);
        book2 = book(2L);
        book3 = book(3L);
        book4 = book(4L);
        List<Book> books = List.of(book1, book2, book3, book4);

        when(bookRepository.findAllById(anyIterable())).thenAnswer(invocation -> {
            List<Book> found = new ArrayList<>();
            for (Object id : (Iterable<?>) invocation.getArgument(0)) {
                books.stream().filter(b -> b.getId().equals(id)).forEach(found::add);
            }
            return found;
        });
    }

    private static Book book(Long id) {
        Book book = new Book("Book " + id, "Author", "Genre", "Description", 2000);
        book.setId(id);
        return book;
    }

    private static Rating rating(Book book, int score, int daysAgo) {
        Rating rating = new Rating(null, book, score);
        rating.setRatedAt(LocalDateTime.now().minusDays(daysAgo));
        return rating;
    }

    private void averages() {
        List<Object[]> rows = List.of(
                new Object[]{1L, 4.5, 4L},
                new Object[]{2L, 4.0, 2L},
                new Object[]{3L, 3.0, 5L});
        when(ratingRepository.findBookAverages()).thenReturn(rows);
    }

    @Test
    void topRatedSkipsBooksWithTooFewRatings() {
        averages();

        List<RecommendedBook> result = service.topRated(null);

        assertEquals(List.of(1L, 3L), result.stream().map(RecommendedBook::id).toList());
        assertEquals(4.5, result.get(0).score(), 0.001);
        assertEquals("Rated 4.5 out of 5 by 4 readers", result.get(0).reason());
    }

    @Test
    void topRatedRespectsTheLimit() {
        averages();

        List<RecommendedBook> result = service.topRated(1);

        assertEquals(List.of(1L), result.stream().map(RecommendedBook::id).toList());
    }

    @Test
    void topRatedIsEmptyWithoutRatings() {
        when(ratingRepository.findBookAverages()).thenReturn(List.of());

        assertTrue(service.topRated(null).isEmpty());
    }

    @Test
    void trendingRanksByRecentRatingsThenAverage() {
        when(ratingRepository.findAll()).thenReturn(List.of(
                rating(book1, 5, 1), rating(book1, 4, 2), rating(book1, 3, 3),
                rating(book2, 5, 1), rating(book2, 5, 2), rating(book2, 5, 3),
                rating(book3, 4, 1)));

        List<RecommendedBook> result = service.trending(null);

        assertEquals(List.of(2L, 1L, 3L), result.stream().map(RecommendedBook::id).toList());
        assertEquals(3.0, result.get(0).score(), 0.001);
        assertEquals("3 ratings in the last 30 days", result.get(0).reason());
        assertEquals("1 rating in the last 30 days", result.get(2).reason());
    }

    @Test
    void trendingIgnoresOldRatings() {
        when(ratingRepository.findAll()).thenReturn(List.of(
                rating(book3, 4, 1),
                rating(book4, 5, 40), rating(book4, 5, 41), rating(book4, 5, 45)));

        List<RecommendedBook> result = service.trending(null);

        assertEquals(List.of(3L), result.stream().map(RecommendedBook::id).toList());
    }

    @Test
    void limitBelowOneIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.topRated(0));
        assertThrows(IllegalArgumentException.class, () -> service.trending(-1));
    }
}