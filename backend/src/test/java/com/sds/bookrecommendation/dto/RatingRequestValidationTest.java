package com.sds.bookrecommendation.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the score limits (1 to 5) and required fields on the rating request DTOs.
 */
class RatingRequestValidationTest {

    private static Validator validator;

    /** Creates the validator once for all tests. */
    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /** Scores from 1 to 5 are accepted. */
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    void validScores_areAccepted(int score) {
        assertTrue(validator.validate(new RatingRequest(1L, 1L, score)).isEmpty());
    }

    /** Scores below 1 or above 5 are rejected. */
    @ParameterizedTest
    @ValueSource(ints = {0, 6, -1, 100})
    void invalidScores_areRejected(int score) {
        assertFalse(validator.validate(new RatingRequest(1L, 1L, score)).isEmpty());
    }

    /** A request without a user id or book id is rejected. */
    @Test
    void missingIds_areRejected() {
        assertFalse(validator.validate(new RatingRequest(null, 1L, 4)).isEmpty());
        assertFalse(validator.validate(new RatingRequest(1L, null, 4)).isEmpty());
    }

    /** The update request uses the same 1 to 5 limits. */
    @Test
    void updateRequest_enforcesScoreLimits() {
        assertTrue(validator.validate(new RatingUpdateRequest(3)).isEmpty());
        assertFalse(validator.validate(new RatingUpdateRequest(0)).isEmpty());
        assertFalse(validator.validate(new RatingUpdateRequest(6)).isEmpty());
    }
}