package com.sds.bookrecommendation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * One answer in the personality quiz: the question and the option the user picked.
 */
public record PersonalityAnswer(
        @NotNull(message = "questionId is required") Integer questionId,
        @NotBlank(message = "optionId is required") String optionId
) {
}