package com.sds.bookrecommendation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Answers to the mood quiz. Allowed values:
 * mood: cozy, thrilling, emotional, funny, dark
 * world: fantasy, real-world, scifi, historical
 * length: short (under 250 pages), medium, long (over 500 pages)
 * romance: true or false
 * tone: light, serious, hopeful, mysterious
 */
public record MoodQuizRequest(
        @NotBlank(message = "mood is required") String mood,
        @NotBlank(message = "world is required") String world,
        @NotBlank(message = "length is required") String length,
        @NotNull(message = "romance is required") Boolean romance,
        @NotBlank(message = "tone is required") String tone
) {
}