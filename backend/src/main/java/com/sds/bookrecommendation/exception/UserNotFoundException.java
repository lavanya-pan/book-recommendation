package com.sds.bookrecommendation.exception;

/**
 * Thrown when a user with the given ID does not exist.
 */
public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(Long id) {
        super("User not found with id " + id);
    }
}
