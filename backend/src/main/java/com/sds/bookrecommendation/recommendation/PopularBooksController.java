package com.sds.bookrecommendation.recommendation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public book lists that do not need a logged-in user, for the home page.
 * Person 1's BookController owns the other /api/books paths.
 */
@RestController
@RequestMapping("/api/books")
public class PopularBooksController {

    private final PopularBooksService popularBooksService;

    public PopularBooksController(PopularBooksService popularBooksService) {
        this.popularBooksService = popularBooksService;
    }

    /** GET /api/books/top-rated?limit=10 */
    @GetMapping("/top-rated")
    public List<RecommendedBook> topRated(@RequestParam(required = false) Integer limit) {
        return popularBooksService.topRated(limit);
    }

    /** GET /api/books/trending?limit=10 */
    @GetMapping("/trending")
    public List<RecommendedBook> trending(@RequestParam(required = false) Integer limit) {
        return popularBooksService.trending(limit);
    }
}