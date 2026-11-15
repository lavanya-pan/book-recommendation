package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.entity.WishlistItem;
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

    @Transactional
    public WishlistItem addBook(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));

        if (wishlistRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new IllegalArgumentException("Book is already in the wishlist");
        }

        WishlistItem item = new WishlistItem(user, book);
        return wishlistRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<WishlistItem> getWishlist(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }

        return wishlistRepository.findByUserIdOrderByAddedAtDesc(userId);
    }

    @Transactional
    public void removeBook(Long userId, Long bookId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }

        if (!wishlistRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new IllegalArgumentException("Book is not in the wishlist");
        }

        wishlistRepository.deleteByUserIdAndBookId(userId, bookId);
    }
}