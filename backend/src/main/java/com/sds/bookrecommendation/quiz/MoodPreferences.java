package com.sds.bookrecommendation.quiz;

import java.util.List;
import java.util.Map;

/**
 * What the mood quiz answers mean: genre weights, keywords, a page range and a romance flag.
 */
public record MoodPreferences(
        Map<String, Double> genreWeights,
        List<String> keywords,
        int minPages,
        int maxPages,
        boolean romance
) {
}