package com.sds.bookrecommendation.dto;

import com.sds.bookrecommendation.entity.Book;

/**
 * Data the API sends back for a book. Controllers return this, never the entity.
 */
public record BookResponse(
        Long id,
        String title,
        String author,
        String genre,
        String description,
        Integer publishedYear,
        String coverUrl,
        Integer pageCount,
        String tags,
        double avgRating,
        int ratingCount
) {

    /** Builds a response from a Book entity, copying every field the frontend needs. */
    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getGenre(),
                book.getDescription(),
                book.getPublishedYear(),
                book.getCoverUrl(),
                book.getPageCount(),
                book.getTags(),
                book.getAvgRating(),
                book.getRatingCount()
        );
    }
}