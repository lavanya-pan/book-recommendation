package com.sds.bookrecommendation.exception;

/**
 * Thrown when a book with the given ID does not exist.
 */
public class BookNotFoundException extends ResourceNotFoundException {

    public BookNotFoundException(Long id) {
        super("Book not found with id " + id);
    }
}
