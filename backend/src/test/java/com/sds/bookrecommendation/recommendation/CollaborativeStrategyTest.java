package com.sds.bookrecommendation.recommendation;

import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.Rating;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CollaborativeStrategyTest {

    private RatingRepository ratingRepository;
    private BookRepository bookRepository;
    private CollaborativeStrategy strategy;

    private Book bookA;
    private Book bookB;
    private Book bookC;
    private Book bookD;
    private Book bookE;
    private User u1;
    private User u2;
    private User u3;
    private User u4;
    private User u5;

    @BeforeEach
    void setUp() {
        ratingRepository = mock(RatingRepository.class);
        bookRepository = mock(BookRepository.class);
        strategy = new CollaborativeStrategy(ratingRepository, bookRepository);

        bookA = book(1L, "Book A");
        bookB = book(2L, "Book B");
        bookC = book(3L, "Book C");
        bookD = book(4L, "Book D");
        bookE = book(5L, "Book E");
        List<Book> books = List.of(bookA, bookB, bookC, bookD, bookE);

        u1 = user(1L);
        u2 = user(2L);
        u3 = user(3L);
        u4 = user(4L);
        u5 = user(5L);

        when(bookRepository.findAllById(anyIterable())).thenAnswer(invocation -> {
            List<Book> found = new ArrayList<>();
            for (Object id : (Iterable<?>) invocation.getArgument(0)) {
                books.stream().filter(b -> b.getId().equals(id)).forEach(found::add);
            }
            return found;
        });
    }

    private static Book book(Long id, String title) {
        Book book = new Book(title, "Author", "Genre", "Description", 2000);
        book.setId(id);
        return book;
    }

    private static User user(Long id) {
        User user = new User("User " + id, "user" + id + "@example.com");
        user.setId(id);
        return user;
    }

    /**
     * User 1 gives Book A the score given. The other readers rate the same way in every test:
     * users 2 and 3 liked A, B and C but disliked E; user 4 liked A and D; user 5 liked C.
     */
    private void ratings(int scoreOfUser1ForA) {
        when(ratingRepository.findAll()).thenReturn(List.of(
                new Rating(u1, bookA, scoreOfUser1ForA),
                new Rating(u2, bookA, 5), new Rating(u2, bookB, 5), new Rating(u2, bookC, 4),
                new Rating(u2, bookE, 1),
                new Rating(u3, bookA, 4), new Rating(u3, bookB, 4), new Rating(u3, bookC, 4),
                new Rating(u3, bookE, 2),
                new Rating(u4, bookA, 5), new Rating(u4, bookD, 5),
                new Rating(u5, bookC, 5)));
    }

    @Test
    void typeIsCollaborative() {
        assertEquals("collaborative", strategy.type());
    }

    @Test
    void ranksBooksLikedByLikeMindedReadersFirst() {
        ratings(5);

        List<RecommendedBook> result = strategy.recommend(1L, 10);

        assertEquals(List.of(2L, 3L), result.stream().map(RecommendedBook::id).toList());
        assertEquals(3.356, result.get(0).score(), 0.01);
        assertEquals(2.499, result.get(1).score(), 0.01);
    }

    @Test
    void ignoresBooksTheNeighboursDidNotLike() {
        ratings(5);

        List<RecommendedBook> result = strategy.recommend(1L, 10);

        assertTrue(result.stream().noneMatch(r -> r.id() == 5L));
    }

    @Test
    void skipsBooksLikedByOnlyOneSharedReader() {
        ratings(5);

        List<RecommendedBook> result = strategy.recommend(1L, 10);

        assertTrue(result.stream().noneMatch(r -> r.id() == 4L));
    }

    @Test
    void neverRecommendsBooksTheUserAlreadyRated() {
        ratings(5);

        List<RecommendedBook> result = strategy.recommend(1L, 10);

        assertTrue(result.stream().noneMatch(r -> r.id() == 1L));
    }

    @Test
    void reasonNamesTheLikedBook() {
        ratings(5);

        List<RecommendedBook> result = strategy.recommend(1L, 10);

        assertTrue(result.get(0).reason().contains("Book A"));
    }

    @Test
    void respectsTheLimit() {
        ratings(5);

        List<RecommendedBook> result = strategy.recommend(1L, 1);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).id());
    }

    @Test
    void returnsEmptyWhenUserLikedNothing() {
        ratings(2);

        assertTrue(strategy.recommend(1L, 10).isEmpty());
    }

    @Test
    void returnsEmptyForUnknownUserNullUserOrBadLimit() {
        ratings(5);

        assertTrue(strategy.recommend(99L, 10).isEmpty());
        assertTrue(strategy.recommend(null, 10).isEmpty());
        assertTrue(strategy.recommend(1L, 0).isEmpty());
    }
}