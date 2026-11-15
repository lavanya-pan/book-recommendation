package com.sds.bookrecommendation.repository;

import com.sds.bookrecommendation.entity.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    /** Returns [bookId, number of interactions] for every book touched since the given time. */
    @Query("SELECT i.book.id, COUNT(i) FROM Interaction i "
            + "WHERE i.createdAt >= :since GROUP BY i.book.id")
    List<Object[]> countByBookSince(@Param("since") LocalDateTime since);
}