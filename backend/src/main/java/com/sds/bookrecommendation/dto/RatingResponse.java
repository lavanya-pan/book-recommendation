package com.sds.bookrecommendation.dto;

import com.sds.bookrecommendation.entity.Rating;

import java.time.LocalDateTime;

/**
 * Data the API sends back for a rating. Controllers return this, never the entity.
 */
public record RatingResponse(
        Long id,
        Long userId,
        Long bookId,
        String bookTitle,
        int score,
        LocalDateTime ratedAt
) {

    /** Builds a response from a Rating entity, copying the fields the frontend needs. */
    public static RatingResponse from(Rating rating) {
        return new RatingResponse(
                rating.getId(),
                rating.getUser().getId(),
                rating.getBook().getId(),
                rating.getBook().getTitle(),
                rating.getScore(),
                rating.getRatedAt());
    }
}