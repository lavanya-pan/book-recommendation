package com.sds.bookrecommendation.repository;

import com.sds.bookrecommendation.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByUserIdAndBookId(Long userId, Long bookId);

    List<Rating> findByUserId(Long userId);

    /** Returns rows of [bookId, averageScore, ratingCount], best average first. */
    @Query("SELECT r.book.id, AVG(r.score), COUNT(r) FROM Rating r "
            + "GROUP BY r.book.id ORDER BY AVG(r.score) DESC, COUNT(r) DESC")
    List<Object[]> findBookAverages();

    /** Returns one page of a user's ratings (used by the Profile page). */
    Page<Rating> findByUserId(Long userId, Pageable pageable);

    /** Returns the average score of one book, or 0 if it has no ratings. */
    @Query("SELECT COALESCE(AVG(r.score), 0) FROM Rating r WHERE r.book.id = :bookId")
    double averageScoreByBookId(@Param("bookId") Long bookId);

    /** Returns how many ratings one book has. */
    long countByBookId(Long bookId);
}
