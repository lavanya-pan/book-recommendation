package com.sds.bookrecommendation.recommendation;

import java.util.List;

/**
 * Common contract for every recommendation type (top-rated, genre,
 * content-based, collaborative, hybrid). The service picks one by its type name.
 */
public interface RecommendationStrategy {

    /** Name used in the request, for example "top-rated" or "genre". */
    String type();

    /**
     * Recommends books for a user.
     *
     * @param userId the user to recommend for
     * @param limit  maximum number of books to return
     * @return books ordered from best to worst
     */
    List<RecommendedBook> recommend(Long userId, int limit);
}