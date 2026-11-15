
package com.sds.bookrecommendation.repository;

import com.sds.bookrecommendation.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findAllByUser_IdOrderByAddedAtDesc(Long userId);

    Optional<WishlistItem> findByUser_IdAndBook_Id(Long userId, Long bookId);
}
