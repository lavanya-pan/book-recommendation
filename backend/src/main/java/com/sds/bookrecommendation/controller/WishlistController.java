
package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.entity.WishlistItem;
import com.sds.bookrecommendation.security.CurrentUser;
import com.sds.bookrecommendation.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    // Add a book to the logged-in user's wishlist
    @PostMapping("/{bookId}")
    public ResponseEntity<WishlistItem> addToWishlist(
            @PathVariable Long bookId) {

        Long userId = CurrentUser.getId();

        WishlistItem item =
                wishlistService.addToWishlist(userId, bookId);

        return ResponseEntity.ok(item);
    }

    // Get the logged-in user's wishlist
    @GetMapping
    public ResponseEntity<List<WishlistItem>> getWishlist() {

        Long userId = CurrentUser.getId();

        return ResponseEntity.ok(
                wishlistService.getWishlist(userId));
    }

    // Remove a book from the logged-in user's wishlist
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> removeFromWishlist(
            @PathVariable Long bookId) {

        Long userId = CurrentUser.getId();

        wishlistService.removeFromWishlist(userId, bookId);

        return ResponseEntity.noContent().build();
    }
}
