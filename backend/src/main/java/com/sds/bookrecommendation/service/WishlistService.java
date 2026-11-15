
package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.entity.WishlistItem;
import com.sds.bookrecommendation.exception.ResourceNotFoundException;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.UserRepository;
import com.sds.bookrecommendation.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public WishlistService(
            WishlistRepository wishlistRepository,
            UserRepository userRepository,
            BookRepository bookRepository) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    // Add a book to a user's wishlist
    @Transactional
    public WishlistItem addToWishlist(Long userId, Long bookId) {

        if (wishlistRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new IllegalArgumentException(
                    "Book is already in the wishlist"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Book not found"));

        WishlistItem item = new WishlistItem();
        item.setUser(user);
        item.setBook(book);

        return wishlistRepository.save(item);
    }

    // Get all books in a user's wishlist
    @Transactional(readOnly = true)
    public List<WishlistItem> getWishlist(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        return wishlistRepository.findByUserId(userId);
    }

    // Remove a book from a user's wishlist
    @Transactional
    public void removeFromWishlist(Long userId, Long bookId) {

        WishlistItem item = wishlistRepository
                .findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found in wishlist"));

        wishlistRepository.delete(item);
    }
}
