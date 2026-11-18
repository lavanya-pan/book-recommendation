package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.dto.WishlistRequest;
import com.sds.bookrecommendation.dto.WishlistResponse;
import com.sds.bookrecommendation.security.CurrentUser;
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

    // Adds a book to the logged-in user's wishlist; any userId sent by the client is ignored.
    @PostMapping
    public ResponseEntity<WishlistResponse> addToWishlist(
            @Valid @RequestBody WishlistRequest request) {

        request.setUserId(CurrentUser.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(wishlistService.addToWishlist(request));
    }

    // Returns the logged-in user's own wishlist.
    @GetMapping
    public ResponseEntity<List<WishlistResponse>> getWishlist() {

        return ResponseEntity.ok(
                wishlistService.getWishlist(CurrentUser.getId()));
    }

    // Removes a book from the logged-in user's own wishlist.
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> removeFromWishlist(
            @PathVariable Long bookId) {

        wishlistService.removeFromWishlist(CurrentUser.getId(), bookId);

        return ResponseEntity.noContent().build();
    }
}
