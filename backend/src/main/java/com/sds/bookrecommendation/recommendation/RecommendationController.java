package com.sds.bookrecommendation.recommendation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for book recommendations.
 * Errors (unknown user, book or type) are turned into JSON by GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /** GET /api/users/1/recommendations?type=genre&limit=10 */
    @GetMapping("/users/{userId}/recommendations")
    public List<RecommendedBook> forUser(@PathVariable Long userId,
                                         @RequestParam(required = false) String type,
                                         @RequestParam(required = false) Integer limit) {
        return recommendationService.recommend(userId, type, limit);
    }

    /** GET /api/books/5/similar?limit=6 */
    @GetMapping("/books/{bookId}/similar")
    public List<RecommendedBook> similar(@PathVariable Long bookId,
                                         @RequestParam(required = false) Integer limit) {
        return recommendationService.similarBooks(bookId, limit);
    }

    /** GET /api/recommendation-types, the names accepted by the type parameter. */
    @GetMapping("/recommendation-types")
    public List<String> types() {
        return recommendationService.availableTypes();
    }
}