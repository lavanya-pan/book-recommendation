
package com.sds.bookrecommendation.recommendation;

import com.sds.bookrecommendation.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/users/{userId}/recommendations")
    public List<RecommendedBook> forUser(
            @PathVariable Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer limit) {

        Long currentUserId = CurrentUser.getId();

        if (!currentUserId.equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access another user's recommendations"
            );
        }

        return recommendationService.recommend(userId, type, limit);
    }

    @GetMapping("/books/{bookId}/similar")
    public List<RecommendedBook> similar(
            @PathVariable Long bookId,
            @RequestParam(required = false) Integer limit) {
        return recommendationService.similarBooks(bookId, limit);
    }

    @GetMapping("/recommendation-types")
    public List<String> types() {
        return recommendationService.availableTypes();
    }
}
