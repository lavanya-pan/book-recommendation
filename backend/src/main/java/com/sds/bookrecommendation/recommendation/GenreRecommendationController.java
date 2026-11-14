package com.sds.bookrecommendation.recommendation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Recommendations for a list of genres, such as the ones from the personality quiz. */
@RestController
@RequestMapping("/api/recommendations")
public class GenreRecommendationController {

    private final GenreRecommendationService service;

    public GenreRecommendationController(GenreRecommendationService service) {
        this.service = service;
    }

    /**
     * Example: GET /api/recommendations/genres?genres=romance,humor&limit=12
     * A missing or empty genres list returns 400.
     */
    @GetMapping("/genres")
    public List<RecommendedBook> byGenres(@RequestParam(required = false) List<String> genres,
                                          @RequestParam(required = false) Integer limit) {
        return service.byGenres(genres, limit);
    }
}