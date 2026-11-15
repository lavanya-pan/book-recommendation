package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.entity.WishlistItem;
import com.sds.bookrecommendation.service.WishlistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    // Add a book to the wishlist
    @PostMapping("/{bookId}")
    public ResponseEntity<?> addBook(
            @PathVariable Long userId,
            @PathVariable Long bookId) {

        try {
            WishlistItem item = wishlistService.addBook(userId, bookId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(toResponse(item));
        } catch (IllegalArgumentException e) {
            HttpStatus status = e.getMessage().contains("already")
                    ? HttpStatus.CONFLICT : HttpStatus.NOT_FOUND;

            return ResponseEntity.status(status)
                    .body(e.getMessage());
        }
    }

    // Get all books in the user's wishlist
    @GetMapping
    public ResponseEntity<?> getWishlist(@PathVariable Long userId) {
        try {
            List<WishlistResponse> wishlist = wishlistService.getWishlist(userId)
                    .stream()
                    .map(this::toResponse)
                    .toList();

            return ResponseEntity.ok(wishlist);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    // Remove a book from the wishlist
    @DeleteMapping("/{bookId}")
    public ResponseEntity<?> removeBook(
            @PathVariable Long userId,
            @PathVariable Long bookId) {

        try {
            wishlistService.removeBook(userId, bookId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    private WishlistResponse toResponse(WishlistItem item) {
        return new WishlistResponse(
                item.getId(),
                item.getBook().getId(),
                item.getBook().getTitle(),
                item.getBook().getAuthor(),
                item.getBook().getGenre(),
                item.getAddedAt()
        );
    }

    public record WishlistResponse(
            Long wishlistId,
            Long bookId,
            String title,
            String author,
            String genre,
            LocalDateTime addedAt
    ) {
    }
}