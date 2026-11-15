
package com.sds.bookrecommendation.repository;

import com.sds.bookrecommendation.entity.ReadingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReadingHistoryRepository
        extends JpaRepository<ReadingHistory, Long> {

    // Get all reading history entries of a user
    List<ReadingHistory> findByUserId(Long userId);

    // Check whether a book already exists in user's history
    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    // Find a particular book in user's reading history
    Optional<ReadingHistory> findByUserIdAndBookId(
            Long userId, Long bookId);
}

