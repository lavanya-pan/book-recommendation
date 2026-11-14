package com.sds.bookrecommendation.exception;

/**
 * Thrown when a rating with the given ID does not exist; handled as HTTP 404.
 */
public class RatingNotFoundException extends ResourceNotFoundException {

    public RatingNotFoundException(Long id) {
        super("Rating not found with id " + id);
    }
}