
package com.sds.bookrecommendation.recommendation;

import com.sds.bookrecommendation.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(
            RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    // Get recommendations for a user
    @GetMapping("/users/{userId}/recommendations")
    public ResponseEntity<?> forUser(
            @PathVariable Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer limit) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        // Check ownership when an authenticated user is available.
        // Spring Security protects this endpoint in the actual application.
        if (authentication != null &&
                authentication.getPrincipal() instanceof User) {

            User loggedInUser = (User) authentication.getPrincipal();

            if (!userId.equals(loggedInUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "message",
                                "You cannot access another user's recommendations"
                        ));
            }
        }

        return ResponseEntity.ok(
                recommendationService.recommend(userId, type, limit)
        );
    }

    // Get similar books
    @GetMapping("/books/{bookId}/similar")
    public List<RecommendedBook> similar(
            @PathVariable Long bookId,
            @RequestParam(required = false) Integer limit) {

        return recommendationService.similarBooks(bookId, limit);
    }

    // Get available recommendation types
    @GetMapping("/recommendation-types")
    public List<String> types() {
        return recommendationService.availableTypes();
    }
}
