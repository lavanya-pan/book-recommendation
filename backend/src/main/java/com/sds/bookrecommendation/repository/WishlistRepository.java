
package com.sds.bookrecommendation.repository;

import com.sds.bookrecommendation.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {

    // Find all wishlist items belonging to a user
    List<WishlistItem> findByUserId(Long userId);

    // Check whether a book is already in the user's wishlist
    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    // Find a specific book in a user's wishlist
    Optional<WishlistItem> findByUserIdAndBookId(Long userId, Long bookId);
}
