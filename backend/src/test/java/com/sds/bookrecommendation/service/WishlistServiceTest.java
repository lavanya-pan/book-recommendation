
package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.dto.WishlistRequest;
import com.sds.bookrecommendation.dto.WishlistResponse;
import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.entity.WishlistItem;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.UserRepository;
import com.sds.bookrecommendation.repository.WishlistRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    private WishlistService service;
    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        service = new WishlistService(
                wishlistRepository,
                userRepository,
                bookRepository
        );

        user = new User("Test User", "test@example.com");
        user.setId(1L);

        book = new Book();
        book.setId(1096L);
        book.setTitle("The Hunger Games");
        book.setAuthor("Suzanne Collins");
        book.setGenre("Fantasy");
    }

    private WishlistRequest createRequest() {
        WishlistRequest request = new WishlistRequest();
        request.setUserId(1L);
        request.setBookId(1096L);
        return request;
    }

    @Test
    void addToWishlist_validRequest_savesItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1096L)).thenReturn(Optional.of(book));
        when(wishlistRepository.findByUser_IdAndBook_Id(1L, 1096L))
                .thenReturn(Optional.empty());
        when(wishlistRepository.save(any(WishlistItem.class)))
                .thenAnswer(call -> call.getArgument(0));

        WishlistResponse response = service.addToWishlist(createRequest());

        assertEquals(1L, response.getUserId());
        assertEquals(1096L, response.getBookId());
        assertEquals("The Hunger Games", response.getBookTitle());

        verify(wishlistRepository).save(any(WishlistItem.class));
    }

    @Test
    void addToWishlist_duplicateItem_throwsConflict() {
        WishlistItem existing = new WishlistItem(user, book);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1096L)).thenReturn(Optional.of(book));
        when(wishlistRepository.findByUser_IdAndBook_Id(1L, 1096L))
                .thenReturn(Optional.of(existing));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.addToWishlist(createRequest())
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(wishlistRepository, never()).save(any());
    }

    @Test
    void addToWishlist_unknownUser_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        WishlistRequest request = createRequest();
        request.setUserId(99L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.addToWishlist(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void addToWishlist_unknownBook_throwsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(9999L)).thenReturn(Optional.empty());

        WishlistRequest request = createRequest();
        request.setBookId(9999L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.addToWishlist(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void getWishlist_validUser_returnsItems() {
        WishlistItem item = new WishlistItem(user, book);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(wishlistRepository.findAllByUser_IdOrderByAddedAtDesc(1L))
                .thenReturn(List.of(item));

        List<WishlistResponse> result = service.getWishlist(1L);

        assertEquals(1, result.size());
        assertEquals("The Hunger Games", result.get(0).getBookTitle());
        assertEquals(1096L, result.get(0).getBookId());
    }

    @Test
    void getWishlist_unknownUser_throwsNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getWishlist(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(wishlistRepository, never())
                .findAllByUser_IdOrderByAddedAtDesc(any());
    }

    @Test
    void removeFromWishlist_existingItem_deletesItem() {
        WishlistItem item = new WishlistItem(user, book);

        when(wishlistRepository.findByUser_IdAndBook_Id(1L, 1096L))
                .thenReturn(Optional.of(item));

        service.removeFromWishlist(1L, 1096L);

        verify(wishlistRepository).delete(item);
    }

    @Test
    void removeFromWishlist_unknownItem_throwsNotFound() {
        when(wishlistRepository.findByUser_IdAndBook_Id(1L, 9999L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.removeFromWishlist(1L, 9999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(wishlistRepository, never()).delete(any());
    }
}
