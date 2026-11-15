
package com.sds.bookrecommendation.repository;

import com.sds.bookrecommendation.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Get all reviews for a particular book
    List<Review> findByBookId(Long bookId);

    // Get all reviews written by a user
    List<Review> findByUserId(Long userId);

    // Check whether a user has already reviewed a book
    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    // Find a user's review for a particular book
    Optional<Review> findByUserIdAndBookId(Long userId, Long bookId);
}
