
package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.dto.RatingRequest;
import com.sds.bookrecommendation.dto.RatingResponse;
import com.sds.bookrecommendation.dto.RatingUpdateRequest;
import com.sds.bookrecommendation.security.CurrentUser;
import com.sds.bookrecommendation.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    // Create a rating using the logged-in user's ID
    @PostMapping("/ratings")
    public ResponseEntity<RatingResponse> rateBook(
            @Valid @RequestBody RatingRequest request) {

        Long currentUserId = CurrentUser.getId();

        RatingResponse response =
                ratingService.rateBook(currentUserId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Update only the logged-in user's rating
    @PutMapping("/ratings/{id}")
    public RatingResponse updateRating(
            @PathVariable Long id,
            @Valid @RequestBody RatingUpdateRequest request) {

        return ratingService.updateRating(
                id,
                CurrentUser.getId(),
                request
        );
    }

    // Delete only the logged-in user's rating
    @DeleteMapping("/ratings/{id}")
    public ResponseEntity<Void> deleteRating(
            @PathVariable Long id) {

        ratingService.deleteRating(
                id,
                CurrentUser.getId()
        );

        return ResponseEntity.noContent().build();
    }

    // Get ratings for a particular user
    @GetMapping("/users/{id}/ratings")
    public Page<RatingResponse> getUserRatings(
            @PathVariable Long id,
            @PageableDefault(
                    size = 10,
                    sort = "ratedAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        return ratingService.getRatingsByUser(id, pageable);
    }
}
