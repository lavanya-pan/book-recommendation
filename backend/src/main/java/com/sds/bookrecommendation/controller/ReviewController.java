
package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.entity.Review;
import com.sds.bookrecommendation.security.CurrentUser;
import com.sds.bookrecommendation.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Request body for submitting a review
    public record ReviewRequest(
            @NotNull Long bookId,
            @NotBlank String text) {
    }

    // Safe response containing review details
    public record ReviewResponse(
            Long id,
            Long userId,
            Long bookId,
            String text,
            String sentimentLabel,
            Double sentimentScore,
            LocalDateTime createdAt) {
    }

    // Add a review as the logged-in user
    @PostMapping("/reviews")
    public ResponseEntity<ReviewResponse> addReview(
            @Valid @RequestBody ReviewRequest request) {

        Long userId = CurrentUser.getId();

        Review review = reviewService.addReview(
                userId,
                request.bookId(),
                request.text());

        return ResponseEntity.ok(toResponse(review));
    }

    // Get all reviews for a particular book
    @GetMapping("/books/{id}/reviews")
    public ResponseEntity<List<ReviewResponse>> getBookReviews(
            @PathVariable Long id) {

        List<ReviewResponse> reviews =
                reviewService.getBookReviews(id)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(reviews);
    }

    // Convert Review entity into a safe response
    private ReviewResponse toResponse(Review review) {

        return new ReviewResponse(
                review.getId(),
                review.getUser().getId(),
                review.getBook().getId(),
                review.getText(),
                review.getSentimentLabel(),
                review.getSentimentScore(),
                review.getCreatedAt());
    }
}
