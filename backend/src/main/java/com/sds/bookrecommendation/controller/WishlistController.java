
package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.dto.WishlistRequest;
import com.sds.bookrecommendation.dto.WishlistResponse;
import com.sds.bookrecommendation.service.WishlistService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    @PostMapping
    public ResponseEntity<WishlistResponse> addToWishlist(
            @Valid @RequestBody WishlistRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(wishlistService.addToWishlist(request));
    }

    @GetMapping
    public ResponseEntity<List<WishlistResponse>> getWishlist(
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                wishlistService.getWishlist(userId));
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> removeFromWishlist(
            @RequestParam Long userId,
            @PathVariable Long bookId) {

        wishlistService.removeFromWishlist(userId, bookId);

        return ResponseEntity.noContent().build();
    }
}
