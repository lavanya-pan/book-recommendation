package com.sds.bookrecommendation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Data the client sends to change an existing rating's score (1 to 5).
 */
public record RatingUpdateRequest(
        @Min(value = 1, message = "score must be at least 1")
        @Max(value = 5, message = "score must be at most 5") int score
) {
}