package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.entity.WishlistItem;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.UserRepository;
import com.sds.bookrecommendation.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private WishlistService wishlistService;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        user = new User("Test User", "test@example.com");

        book = new Book();
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setGenre("Fiction");
    }

    @Test
    void addBook_returnsSavedWishlistItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(5L)).thenReturn(Optional.of(book));
        when(wishlistRepository.existsByUserIdAndBookId(1L, 5L))
                .thenReturn(false);
        when(wishlistRepository.save(any(WishlistItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WishlistItem result = wishlistService.addBook(1L, 5L);

        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals(book, result.getBook());

        verify(wishlistRepository).save(any(WishlistItem.class));
    }

    @Test
    void addBook_duplicateBook_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(5L)).thenReturn(Optional.of(book));
        when(wishlistRepository.existsByUserIdAndBookId(1L, 5L))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> wishlistService.addBook(1L, 5L));

        verify(wishlistRepository, never()).save(any(WishlistItem.class));
    }

    @Test
    void getWishlist_returnsUserWishlist() {
        WishlistItem item = new WishlistItem(user, book);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(wishlistRepository.findByUserIdOrderByAddedAtDesc(1L))
                .thenReturn(List.of(item));

        List<WishlistItem> result = wishlistService.getWishlist(1L);

        assertEquals(1, result.size());
        assertEquals(book, result.get(0).getBook());
    }

    @Test
    void removeBook_deletesWishlistItem() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(wishlistRepository.existsByUserIdAndBookId(1L, 5L))
                .thenReturn(true);

        wishlistService.removeBook(1L, 5L);

        verify(wishlistRepository).deleteByUserIdAndBookId(1L, 5L);
    }
}