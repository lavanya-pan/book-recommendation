
package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.dto.ReviewRequest;
import com.sds.bookrecommendation.dto.ReviewResponse;
import com.sds.bookrecommendation.service.ReviewService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/reviews")
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reviewService.createReview(request));
    }

    @GetMapping("/books/{bookId}/reviews")
    public ResponseEntity<List<ReviewResponse>> getReviewsByBook(
            @PathVariable Long bookId) {

        return ResponseEntity.ok(
                reviewService.getReviewsByBook(bookId)
        );
    }
}
