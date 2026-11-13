package com.sds.bookrecommendation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data a client sends to create or update a book.
 * The rating fields are left out on purpose: the rating module owns them.
 */
public record BookRequest(

        @NotBlank(message = "must not be blank")
        @Size(max = 255, message = "must be at most 255 characters")
        String title,

        @NotBlank(message = "must not be blank")
        @Size(max = 255, message = "must be at most 255 characters")
        String author,

        @NotBlank(message = "must not be blank")
        @Size(max = 255, message = "must be at most 255 characters")
        String genre,

        @Size(max = 2000, message = "must be at most 2000 characters")
        String description,

        @Min(value = -3000, message = "must be -3000 or later")
        @Max(value = 2100, message = "must be 2100 or earlier")
        Integer publishedYear,

        @Size(max = 500, message = "must be at most 500 characters")
        String coverUrl,

        @Min(value = 1, message = "must be at least 1")
        @Max(value = 10000, message = "must be at most 10000")
        Integer pageCount,

        @Size(max = 1000, message = "must be at most 1000 characters")
        String tags
) {
}