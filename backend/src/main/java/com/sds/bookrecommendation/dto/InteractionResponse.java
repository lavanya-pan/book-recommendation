package com.sds.bookrecommendation.dto;

import com.sds.bookrecommendation.entity.Interaction;
import com.sds.bookrecommendation.entity.InteractionType;

import java.time.LocalDateTime;

/**
 * Data the API sends back for a logged interaction.
 */
public record InteractionResponse(
        Long id,
        Long userId,
        Long bookId,
        InteractionType type,
        LocalDateTime createdAt
) {

    /** Builds a response from an Interaction entity. */
    public static InteractionResponse from(Interaction interaction) {
        return new InteractionResponse(
                interaction.getId(),
                interaction.getUser().getId(),
                interaction.getBook().getId(),
                interaction.getType(),
                interaction.getCreatedAt());
    }
}