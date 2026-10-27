package com.sds.bookrecommendation.recommendation;

import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.Rating;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.exception.BookNotFoundException;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ContentBasedStrategyTest {

    private RatingRepository ratingRepository;
    private BookRepository bookRepository;
    private ContentBasedStrategy strategy;
    private List<Book> books;
    private User user;

    @BeforeEach
    void setUp() {
        ratingRepository = mock(RatingRepository.class);
        bookRepository = mock(BookRepository.class);
        strategy = new ContentBasedStrategy(ratingRepository, bookRepository);

        books = List.of(
                book(1L, "Dragon Quest", "Fantasy", "dragon wizard magic kingdom fantasy"),
                book(2L, "Dragon Sword", "Fantasy", "dragon wizard magic sword fantasy"),
                book(3L, "London Murder", "Mystery", "detective murder mystery london"),
                book(4L, "Wedding Hearts", "Romance", "love romance wedding heart"),
                book(5L, "Wizard School", "Fantasy", "wizard school magic fantasy"));

        user = new User("Test", "test@example.com");
        user.setId(1L);

        when(bookRepository.count()).thenReturn((long) books.size());
        when(bookRepository.findAll()).thenReturn(books);
        when(bookRepository.findAllById(anyIterable())).thenAnswer(invocation -> {
            List<Book> found = new ArrayList<>();
            for (Object id : (Iterable<?>) invocation.getArgument(0)) {
                books.stream().filter(b -> b.getId().equals(id)).forEach(found::add);
            }
            return found;
        });
    }

    private static Book book(Long id, String title, String genre, String description) {
        Book book = new Book(title, "Author", genre, description, 2000);
        book.setId(id);
        return book;
    }

    @Test
    void typeIsContent() {
        assertEquals("content", strategy.type());
    }

    @Test
    void recommendsSimilarBooksAndSkipsRatedOnes() {
        when(ratingRepository.findByUserId(1L)).thenReturn(List.of(new Rating(user, books.get(0), 5)));

        List<RecommendedBook> result = strategy.recommend(1L, 5);

        assertFalse(result.isEmpty());
        assertEquals(2L, result.get(0).id());
        assertTrue(result.stream().noneMatch(r -> r.id() == 1L));
        assertEquals(5L, result.get(1).id());
    }

    @Test
    void ignoresBooksThatOnlyShareAStrayWord() {
        when(ratingRepository.findByUserId(1L)).thenReturn(List.of(new Rating(user, books.get(0), 5)));

        List<RecommendedBook> result = strategy.recommend(1L, 5);

        assertTrue(result.stream().noneMatch(r -> r.id() == 3L || r.id() == 4L));
    }

    @Test
    void reasonMentionsTheLikedBook() {
        when(ratingRepository.findByUserId(1L)).thenReturn(List.of(new Rating(user, books.get(0), 5)));

        List<RecommendedBook> result = strategy.recommend(1L, 5);

        assertTrue(result.get(0).reason().contains("Dragon Quest"));
    }

    @Test
    void returnsEmptyWhenUserLikedNothing() {
        when(ratingRepository.findByUserId(1L)).thenReturn(List.of(new Rating(user, books.get(0), 2)));

        assertTrue(strategy.recommend(1L, 5).isEmpty());
    }

    @Test
    void returnsEmptyForUserWithNoRatingsNullUserOrBadLimit() {
        when(ratingRepository.findByUserId(1L)).thenReturn(List.of());

        assertTrue(strategy.recommend(1L, 5).isEmpty());
        assertTrue(strategy.recommend(null, 5).isEmpty());
        assertTrue(strategy.recommend(1L, 0).isEmpty());
    }

    @Test
    void similarBooksRanksBestMatchFirst() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(books.get(0)));

        List<RecommendedBook> result = strategy.similarBooks(1L, 3);

        assertEquals(2L, result.get(0).id());
        assertTrue(result.stream().noneMatch(r -> r.id() == 1L));
    }

    @Test
    void similarBooksThrowsForUnknownBook() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> strategy.similarBooks(99L, 3));
    }
}