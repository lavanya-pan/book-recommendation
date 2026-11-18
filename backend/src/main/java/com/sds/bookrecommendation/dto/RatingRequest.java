package com.sds.bookrecommendation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Data the client sends to rate a book: who rates, which book, and the score (1 to 5).
 */
public record RatingRequest(
        @NotNull(message = "userId is required") Long userId,
        @NotNull(message = "bookId is required") Long bookId,
        @Min(value = 1, message = "score must be at least 1")
        @Max(value = 5, message = "score must be at most 5") int score
) {
}