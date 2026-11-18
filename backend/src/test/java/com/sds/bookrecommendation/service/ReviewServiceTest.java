
package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.dto.ReviewRequest;
import com.sds.bookrecommendation.dto.ReviewResponse;
import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.Review;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.exception.BookNotFoundException;
import com.sds.bookrecommendation.exception.UserNotFoundException;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.ReviewRepository;
import com.sds.bookrecommendation.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void createReviewSuccessfully() {

        User user = mock(User.class);
        Book book = mock(Book.class);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(1096L))
                .thenReturn(Optional.of(book));

        when(user.getId()).thenReturn(1L);
        when(user.getName()).thenReturn("Test User");

        when(book.getId()).thenReturn(1096L);
        when(book.getTitle()).thenReturn("The Hunger Games");

        when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReviewRequest request = new ReviewRequest(
                1L,
                1096L,
                "This is an excellent book!"
        );

        ReviewResponse response = reviewService.createReview(request);

        assertEquals(1L, response.userId());
        assertEquals(1096L, response.bookId());
        assertEquals("Test User", response.userName());
        assertEquals("The Hunger Games", response.bookTitle());
        assertEquals("This is an excellent book!", response.text());
        assertNotNull(response.createdAt());

        ArgumentCaptor<Review> captor =
                ArgumentCaptor.forClass(Review.class);

        verify(reviewRepository).save(captor.capture());

        assertEquals(
                "This is an excellent book!",
                captor.getValue().getText()
        );
    }

    @Test
    void createReviewWhenUserDoesNotExist() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        ReviewRequest request = new ReviewRequest(
                1L,
                1096L,
                "Great book!"
        );

        assertThrows(
                UserNotFoundException.class,
                () -> reviewService.createReview(request)
        );

        verify(reviewRepository, never()).save(any());
        verifyNoInteractions(bookRepository);
    }

    @Test
    void createReviewWhenBookDoesNotExist() {

        User user = mock(User.class);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(1096L))
                .thenReturn(Optional.empty());

        ReviewRequest request = new ReviewRequest(
                1L,
                1096L,
                "Great book!"
        );

        assertThrows(
                BookNotFoundException.class,
                () -> reviewService.createReview(request)
        );

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void getReviewsByBookSuccessfully() {

        User user = mock(User.class);
        Book book = mock(Book.class);

        when(user.getId()).thenReturn(1L);
        when(user.getName()).thenReturn("Test User");

        when(book.getId()).thenReturn(1096L);
        when(book.getTitle()).thenReturn("The Hunger Games");

        when(bookRepository.existsById(1096L))
                .thenReturn(true);

        Review review = new Review(
                user,
                book,
                "Amazing story!"
        );

        when(reviewRepository.findByBook_IdOrderByCreatedAtDesc(1096L))
                .thenReturn(List.of(review));

        List<ReviewResponse> responses =
                reviewService.getReviewsByBook(1096L);

        assertEquals(1, responses.size());
        assertEquals("Amazing story!", responses.get(0).text());
        assertEquals("Test User", responses.get(0).userName());
        assertEquals("The Hunger Games", responses.get(0).bookTitle());
    }

    @Test
    void getReviewsWhenBookDoesNotExist() {

        when(bookRepository.existsById(9999L))
                .thenReturn(false);

        assertThrows(
                BookNotFoundException.class,
                () -> reviewService.getReviewsByBook(9999L)
        );

        verify(reviewRepository, never())
                .findByBook_IdOrderByCreatedAtDesc(anyLong());
    }
}
