
package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.dto.WishlistRequest;
import com.sds.bookrecommendation.dto.WishlistResponse;
import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.entity.WishlistItem;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.UserRepository;
import com.sds.bookrecommendation.repository.WishlistRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    public WishlistResponse addToWishlist(WishlistRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Book not found"));

        if (wishlistRepository.findByUser_IdAndBook_Id(
                request.getUserId(), request.getBookId()).isPresent()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Book already exists in wishlist");
        }

        WishlistItem item = new WishlistItem(user, book);

        return WishlistResponse.from(wishlistRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<WishlistResponse> getWishlist(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User not found");
        }

        return wishlistRepository
                .findAllByUser_IdOrderByAddedAtDesc(userId)
                .stream()
                .map(WishlistResponse::from)
                .toList();
    }

    @Transactional
    public void removeFromWishlist(Long userId, Long bookId) {

        WishlistItem item = wishlistRepository
                .findByUser_IdAndBook_Id(userId, bookId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Wishlist item not found"));

        wishlistRepository.delete(item);
    }
}
