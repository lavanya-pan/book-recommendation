
package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.dto.RatingRequest;
import com.sds.bookrecommendation.dto.RatingResponse;
import com.sds.bookrecommendation.dto.RatingUpdateRequest;
import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.Rating;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.exception.BookNotFoundException;
import com.sds.bookrecommendation.exception.DuplicateRatingException;
import com.sds.bookrecommendation.exception.RatingNotFoundException;
import com.sds.bookrecommendation.exception.UserNotFoundException;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.RatingRepository;
import com.sds.bookrecommendation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    private RatingService service;
    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        service = new RatingService(
                ratingRepository,
                bookRepository,
                userRepository
        );

        user = new User("Test User", "test@example.com");
        user.setId(1L);

        book = new Book();
        book.setId(1L);
        book.setTitle("Test Book One");
        book.setAuthor("Author A");
        book.setGenre("fantasy");
    }

    private void givenStats(double average, long count) {
        when(ratingRepository.averageScoreByBookId(1L))
                .thenReturn(average);
        when(ratingRepository.countByBookId(1L))
                .thenReturn(count);
    }

    private Rating givenExistingRating(int score) {
        Rating rating = new Rating(user, book, score);
        rating.setId(10L);

        when(ratingRepository.findById(10L))
                .thenReturn(Optional.of(rating));

        return rating;
    }

    @Test
    void rateBook_validRequest_savesAndUpdatesBookStats() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));
        when(ratingRepository.findByUserIdAndBookId(1L, 1L))
                .thenReturn(Optional.empty());
        when(ratingRepository.save(any(Rating.class)))
                .thenAnswer(call -> call.getArgument(0));

        givenStats(4.0, 1);

        RatingResponse response = service.rateBook(
                1L,
                new RatingRequest(1L, 1L, 4)
        );

        assertEquals(4, response.score());
        assertEquals(1L, response.userId());
        assertEquals(1L, response.bookId());
        assertEquals(4.0, book.getAvgRating(), 0.0001);
        assertEquals(1, book.getRatingCount());

        verify(bookRepository).save(book);
    }

    @Test
    void rateBook_ignoresDifferentUserIdInRequest() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));
        when(ratingRepository.findByUserIdAndBookId(1L, 1L))
                .thenReturn(Optional.empty());
        when(ratingRepository.save(any(Rating.class)))
                .thenAnswer(call -> call.getArgument(0));

        givenStats(4.0, 1);

        RatingResponse response = service.rateBook(
                1L,
                new RatingRequest(99L, 1L, 4)
        );

        assertEquals(1L, response.userId());
    }

    @Test
    void rateBook_duplicate_throwsAndSavesNothing() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));
        when(ratingRepository.findByUserIdAndBookId(1L, 1L))
                .thenReturn(Optional.of(new Rating(user, book, 3)));

        assertThrows(
                DuplicateRatingException.class,
                () -> service.rateBook(
                        1L,
                        new RatingRequest(1L, 1L, 5)
                )
        );

        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    void rateBook_unknownUser_throws() {
        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> service.rateBook(
                        99L,
                        new RatingRequest(99L, 1L, 4)
                )
        );
    }

    @Test
    void rateBook_unknownBook_throws() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(bookRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                BookNotFoundException.class,
                () -> service.rateBook(
                        1L,
                        new RatingRequest(1L, 99L, 4)
                )
        );
    }

    @Test
    void updateRating_changesScoreAndRecomputesAverage() {
        givenExistingRating(5);

        when(ratingRepository.save(any(Rating.class)))
                .thenAnswer(call -> call.getArgument(0));

        givenStats(2.0, 1);

        RatingResponse response = service.updateRating(
                10L,
                1L,
                new RatingUpdateRequest(2)
        );

        assertEquals(2, response.score());
        assertEquals(2.0, book.getAvgRating(), 0.0001);
        assertEquals(1, book.getRatingCount());
    }

    @Test
    void updateRating_roundsAverageToTwoDecimals() {
        givenExistingRating(3);

        when(ratingRepository.save(any(Rating.class)))
                .thenAnswer(call -> call.getArgument(0));

        givenStats(3.3333333, 3);

        service.updateRating(
                10L,
                1L,
                new RatingUpdateRequest(4)
        );

        assertEquals(3.33, book.getAvgRating(), 0.0001);
        assertEquals(3, book.getRatingCount());
    }

    @Test
    void updateRating_otherUsersRating_returnsForbidden() {
        User anotherUser = new User(
                "Another User",
                "other@example.com"
        );
        anotherUser.setId(2L);

        Rating rating = new Rating(anotherUser, book, 4);
        rating.setId(10L);

        when(ratingRepository.findById(10L))
                .thenReturn(Optional.of(rating));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateRating(
                        10L,
                        1L,
                        new RatingUpdateRequest(2)
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    void updateRating_unknownId_throws() {
        when(ratingRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RatingNotFoundException.class,
                () -> service.updateRating(
                        99L,
                        1L,
                        new RatingUpdateRequest(3)
                )
        );
    }

    @Test
    void deleteRating_removesRatingAndRecomputesStats() {
        Rating rating = givenExistingRating(4);

        book.setAvgRating(4.0);
        book.setRatingCount(1);

        givenStats(0.0, 0);

        service.deleteRating(10L, 1L);

        verify(ratingRepository).delete(rating);

        assertEquals(0.0, book.getAvgRating(), 0.0001);
        assertEquals(0, book.getRatingCount());
    }

    @Test
    void deleteRating_otherUsersRating_returnsForbidden() {
        User anotherUser = new User(
                "Another User",
                "other@example.com"
        );
        anotherUser.setId(2L);

        Rating rating = new Rating(anotherUser, book, 4);
        rating.setId(10L);

        when(ratingRepository.findById(10L))
                .thenReturn(Optional.of(rating));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.deleteRating(10L, 1L)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(ratingRepository, never()).delete(any(Rating.class));
    }

    @Test
    void deleteRating_unknownId_throws() {
        when(ratingRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RatingNotFoundException.class,
                () -> service.deleteRating(99L, 1L)
        );

        verify(ratingRepository, never()).delete(any(Rating.class));
    }

    @Test
    void getRatingsByUser_unknownUser_throws() {
        when(userRepository.existsById(99L))
                .thenReturn(false);

        assertThrows(
                UserNotFoundException.class,
                () -> service.getRatingsByUser(
                        99L,
                        PageRequest.of(0, 10)
                )
        );

        verify(ratingRepository, never())
                .findByUserId(any(Long.class), any(Pageable.class));
    }

    @Test
    void getRatingsByUser_returnsPageOfResponses() {
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.existsById(1L))
                .thenReturn(true);

        when(ratingRepository.findByUserId(1L, pageable))
                .thenReturn(new PageImpl<>(
                        List.of(new Rating(user, book, 4))
                ));

        Page<RatingResponse> page =
                service.getRatingsByUser(1L, pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals(4, page.getContent().get(0).score());
    }
}
