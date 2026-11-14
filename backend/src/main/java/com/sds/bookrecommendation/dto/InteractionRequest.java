package com.sds.bookrecommendation.dto;

import com.sds.bookrecommendation.entity.InteractionType;
import jakarta.validation.constraints.NotNull;

/**
 * Data the client sends to log an action: who, which book, and the kind of action.
 */
public record InteractionRequest(
        @NotNull(message = "userId is required") Long userId,
        @NotNull(message = "bookId is required") Long bookId,
        @NotNull(message = "type is required") InteractionType type
) {
}