
package com.sds.bookrecommendation.repository;

import com.sds.bookrecommendation.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByBook_IdOrderByCreatedAtDesc(Long bookId);
}
