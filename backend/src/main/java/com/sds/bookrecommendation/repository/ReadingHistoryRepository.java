package com.sds.bookrecommendation.repository;

import com.sds.bookrecommendation.entity.ReadingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReadingHistoryRepository
        extends JpaRepository<ReadingHistory, Long> {

    List<ReadingHistory> findAllByUser_IdOrderByUpdatedAtDesc(Long userId);

    Optional<ReadingHistory> findByUser_IdAndBook_Id(
            Long userId, Long bookId
    );
}