package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.dto.RatingImportSummary;
import com.sds.bookrecommendation.service.RatingImportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only endpoint that loads sample ratings into the database.
 */
@RestController
@RequestMapping("/api/admin/import")
public class AdminRatingImportController {

    private final RatingImportService ratingImportService;

    public AdminRatingImportController(RatingImportService ratingImportService) {
        this.ratingImportService = ratingImportService;
    }

    /** POST /api/admin/import/ratings?maxUsers=3: imports the ratings of dataset users 1 to maxUsers. */
    @PostMapping("/ratings")
    public RatingImportSummary importRatings(@RequestParam(defaultValue = "1000") int maxUsers) {
        return ratingImportService.importRatings(maxUsers);
    }
}