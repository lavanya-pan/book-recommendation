
package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.Rating;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.InteractionRepository;
import com.sds.bookrecommendation.repository.RatingRepository;
import com.sds.bookrecommendation.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private InteractionRepository interactionRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    @Test
    void deleteAccount_deletesUserDataAndUpdatesBookStatistics() {

        Long userId = 7L;
        Long bookId = 10L;

        User user = mock(User.class);
        Rating rating = mock(Rating.class);
        Book book = mock(Book.class);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(ratingRepository.findByUserId(userId))
                .thenReturn(List.of(rating));

        when(rating.getBook()).thenReturn(book);
        when(book.getId()).thenReturn(bookId);

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(ratingRepository.averageScoreByBookId(bookId))
                .thenReturn(4.5);

        when(ratingRepository.countByBookId(bookId))
                .thenReturn(2L);

        userProfileService.deleteAccount(userId);

        verify(interactionRepository).deleteByUserId(userId);
        verify(userRepository).delete(user);
        verify(userRepository).flush();

        verify(book).setAvgRating(4.5);
        verify(book).setRatingCount(2);
        verify(bookRepository).save(book);
    }

    @Test
    void deleteAccount_whenUserHasNoRatings_stillDeletesAccount() {

        Long userId = 7L;
        User user = mock(User.class);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(ratingRepository.findByUserId(userId))
                .thenReturn(List.of());

        userProfileService.deleteAccount(userId);

        verify(interactionRepository).deleteByUserId(userId);
        verify(userRepository).delete(user);
        verify(userRepository).flush();

        verifyNoInteractions(bookRepository);
    }
}
