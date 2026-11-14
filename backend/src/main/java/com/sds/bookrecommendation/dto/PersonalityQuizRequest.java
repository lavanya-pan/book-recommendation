package com.sds.bookrecommendation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Data the client sends to submit a personality quiz: one answer per question.
 */
public record PersonalityQuizRequest(
        @NotEmpty(message = "answers are required") List<@Valid PersonalityAnswer> answers
) {
}