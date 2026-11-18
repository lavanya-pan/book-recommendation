
package com.sds.bookrecommendation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Book ID is required")
        Long bookId,

        @NotBlank(message = "Review text cannot be empty")
        @Size(max = 2000, message = "Review cannot exceed 2000 characters")
        String text
) {
}
