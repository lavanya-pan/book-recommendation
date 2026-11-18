package com.sds.bookrecommendation.recommendation;

import com.sds.bookrecommendation.repository.RatingRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Combines content-based and collaborative recommendations:
 * score = alpha * content + (1 - alpha) * collaborative.
 * Alpha is high for users with few ratings (little reader overlap to learn from)
 * and low for users with many ratings.
 */
@Component
public class HybridStrategy implements RecommendationStrategy {

    /** Each engine is asked for at least this many candidates so the two lists can mix. */
    static final int MIN_POOL = 30;

    private final ContentBasedStrategy contentStrategy;
    private final CollaborativeStrategy collaborativeStrategy;
    private final RatingRepository ratingRepository;

    public HybridStrategy(ContentBasedStrategy contentStrategy,
                          CollaborativeStrategy collaborativeStrategy,
                          RatingRepository ratingRepository) {
        this.contentStrategy = contentStrategy;
        this.collaborativeStrategy = collaborativeStrategy;
        this.ratingRepository = ratingRepository;
    }

    /** Name used in the request parameter. */
    @Override
    public String type() {
        return "hybrid";
    }

    /** Weight given to the content-based score for a user who has rated this many books. */
    static double contentWeight(int ratingCount) {
        if (ratingCount < 5) {
            return 0.9;
        }
        if (ratingCount < 20) {
            return 0.6;
        }
        return 0.3;
    }

    /**
     * Returns up to {@code limit} books ranked by the combined score.
     * Returns an empty list when neither engine has anything to recommend.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RecommendedBook> recommend(Long userId, int limit) {
        if (userId == null || limit <= 0) {
            return List.of();
        }

        double alpha = contentWeight(ratingRepository.findByUserId(userId).size());
        int pool = Math.max(limit * 3, MIN_POOL);
        List<RecommendedBook> fromContent = contentStrategy.recommend(userId, pool);
        List<RecommendedBook> fromCollaborative = collaborativeStrategy.recommend(userId, pool);

        Map<Long, RecommendedBook> contentById = byId(fromContent);
        Map<Long, RecommendedBook> collaborativeById = byId(fromCollaborative);
        Map<Long, Double> contentScores = scaled(fromContent);
        Map<Long, Double> collaborativeScores = scaled(fromCollaborative);

        Set<Long> ids = new HashSet<>(contentById.keySet());
        ids.addAll(collaborativeById.keySet());

        List<RecommendedBook> combined = new java.util.ArrayList<>();
        for (Long id : ids) {
            double fromC = alpha * contentScores.getOrDefault(id, 0.0);
            double fromK = (1 - alpha) * collaborativeScores.getOrDefault(id, 0.0);
            RecommendedBook source = (fromC >= fromK && contentById.containsKey(id))
                    ? contentById.get(id) : collaborativeById.get(id);
            double score = Math.round((fromC + fromK) * 1000) / 1000.0;
            combined.add(new RecommendedBook(source.id(), source.title(), source.author(),
                    source.genre(), score, source.reason()));
        }

        combined.sort((a, b) -> {
            int byScore = Double.compare(b.score(), a.score());
            return byScore != 0 ? byScore : Long.compare(a.id(), b.id());
        });
        return combined.size() > limit ? List.copyOf(combined.subList(0, limit)) : combined;
    }

    private static Map<Long, RecommendedBook> byId(List<RecommendedBook> books) {
        Map<Long, RecommendedBook> map = new HashMap<>();
        for (RecommendedBook book : books) {
            map.put(book.id(), book);
        }
        return map;
    }

    /** Scales scores so the best one is 1.0, so the two engines can be compared fairly. */
    private static Map<Long, Double> scaled(List<RecommendedBook> books) {
        double max = 0;
        for (RecommendedBook book : books) {
            max = Math.max(max, book.score());
        }
        Map<Long, Double> scores = new HashMap<>();
        if (max <= 0) {
            return scores;
        }
        for (RecommendedBook book : books) {
            scores.put(book.id(), book.score() / max);
        }
        return scores;
    }
}