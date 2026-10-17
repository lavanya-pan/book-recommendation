package com.sds.bookrecommendation.repository;

import com.sds.bookrecommendation.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByUserIdAndBookId(Long userId, Long bookId);

    List<Rating> findByUserId(Long userId);

    /** Returns rows of [bookId, averageScore, ratingCount], best average first. */
    @Query("SELECT r.book.id, AVG(r.score), COUNT(r) FROM Rating r "
            + "GROUP BY r.book.id ORDER BY AVG(r.score) DESC, COUNT(r) DESC")
    List<Object[]> findBookAverages();
}
