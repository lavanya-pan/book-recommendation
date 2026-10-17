package com.sds.bookrecommendation.exception;

/**
 * Base class for "record not found" errors; handled as HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
