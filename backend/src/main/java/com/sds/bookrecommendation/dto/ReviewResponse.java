
package com.sds.bookrecommendation.dto;

import com.sds.bookrecommendation.entity.Review;
import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long userId,
        String userName,
        Long bookId,
        String bookTitle,
        String text,
        LocalDateTime createdAt
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getUser().getId(),
                review.getUser().getName(),
                review.getBook().getId(),
                review.getBook().getTitle(),
                review.getText(),
                review.getCreatedAt()
        );
    }
}
