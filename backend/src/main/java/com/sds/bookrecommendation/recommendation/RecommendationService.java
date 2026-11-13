package com.sds.bookrecommendation.recommendation;

import com.sds.bookrecommendation.exception.UserNotFoundException;
import com.sds.bookrecommendation.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Picks the recommendation strategy requested by name and runs it.
 * New users with nothing to base a recommendation on get the top-rated books instead.
 */
@Service
public class RecommendationService {

    static final int DEFAULT_LIMIT = 10;
    static final int MAX_LIMIT = 50;
    static final String DEFAULT_TYPE = "content";
    static final String FALLBACK_TYPE = "top-rated";

    private final Map<String, RecommendationStrategy> strategies = new TreeMap<>();
    private final ContentBasedStrategy contentBasedStrategy;
    private final UserRepository userRepository;

    public RecommendationService(List<RecommendationStrategy> strategyList,
                                 ContentBasedStrategy contentBasedStrategy,
                                 UserRepository userRepository) {
        for (RecommendationStrategy strategy : strategyList) {
            strategies.put(strategy.type(), strategy);
        }
        this.contentBasedStrategy = contentBasedStrategy;
        this.userRepository = userRepository;
    }

    /** Names of all strategies that can be requested, in alphabetical order. */
    public List<String> availableTypes() {
        return List.copyOf(strategies.keySet());
    }

    /**
     * Recommendations for one user.
     *
     * @param type  strategy name; blank means "content"
     * @param limit number of books wanted; null means 10, values above 50 are reduced to 50
     * @throws UserNotFoundException    if the user does not exist
     * @throws IllegalArgumentException if the type is unknown or the limit is below 1
     */
    public List<RecommendedBook> recommend(Long userId, String type, Integer limit) {
        if (userId == null || !userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        String name = (type == null || type.isBlank()) ? DEFAULT_TYPE : type.trim().toLowerCase();
        RecommendationStrategy strategy = strategies.get(name);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown recommendation type \"" + name
                    + "\". Use one of: " + String.join(", ", strategies.keySet()));
        }

        int count = resolveLimit(limit);
        List<RecommendedBook> result = strategy.recommend(userId, count);
        if (result.isEmpty() && !name.equals(FALLBACK_TYPE)) {
            RecommendationStrategy fallback = strategies.get(FALLBACK_TYPE);
            if (fallback != null) {
                return fallback.recommend(userId, count);
            }
        }
        return result;
    }

    /**
     * Books similar to the given book, for the "similar books" row on a book page.
     *
     * @throws com.sds.bookrecommendation.exception.BookNotFoundException if the book does not exist
     */
    public List<RecommendedBook> similarBooks(Long bookId, Integer limit) {
        return contentBasedStrategy.similarBooks(bookId, resolveLimit(limit));
    }

    private static int resolveLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be at least 1");
        }
        return Math.min(limit, MAX_LIMIT);
    }
}