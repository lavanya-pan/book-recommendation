package com.sds.bookrecommendation.exception;

/**
 * Thrown when a user tries to rate the same book twice; handled as HTTP 409.
 */
public class DuplicateRatingException extends RuntimeException {

    public DuplicateRatingException(Long userId, Long bookId) {
        super("User " + userId + " has already rated book " + bookId);
    }
}
