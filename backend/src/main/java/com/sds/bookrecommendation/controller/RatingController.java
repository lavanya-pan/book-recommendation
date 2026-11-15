package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.dto.RatingRequest;
import com.sds.bookrecommendation.dto.RatingResponse;
import com.sds.bookrecommendation.dto.RatingUpdateRequest;
import com.sds.bookrecommendation.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for ratings. All paths start with /api.
 */
@RestController
@RequestMapping("/api")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    /** POST /api/ratings: a user rates a book. Returns 201, or 409 if already rated. */
    @PostMapping("/ratings")
    public ResponseEntity<RatingResponse> rateBook(@Valid @RequestBody RatingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.rateBook(request));
    }

    /** PUT /api/ratings/{id}: changes the score of an existing rating. */
    @PutMapping("/ratings/{id}")
    public RatingResponse updateRating(@PathVariable Long id,
                                       @Valid @RequestBody RatingUpdateRequest request) {
        return ratingService.updateRating(id, request);
    }

    /** DELETE /api/ratings/{id}: removes a rating. Returns 204 with no body. */
    @DeleteMapping("/ratings/{id}")
    public ResponseEntity<Void> deleteRating(@PathVariable Long id) {
        ratingService.deleteRating(id);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/users/{id}/ratings: one page of a user's ratings, newest first (page, size, sort). */
    @GetMapping("/users/{id}/ratings")
    public Page<RatingResponse> getUserRatings(
            @PathVariable Long id,
            @PageableDefault(size = 10, sort = "ratedAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ratingService.getRatingsByUser(id, pageable);
    }
}