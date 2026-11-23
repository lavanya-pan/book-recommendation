package com.sds.bookrecommendation.recommendation;

import com.sds.bookrecommendation.entity.Rating;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.RatingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Offline evaluation of the recommendation methods on the REAL imported ratings in the local database.
 * Only runs when the file ~/.run-real-eval exists, so normal test runs skip it (and never touch MySQL).
 * Read-only: schema changes are switched off. Run it on its own and read the table in the console.
 */
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=never",
        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect"})@ActiveProfiles("local")
@Transactional
@EnabledIf("switchOn")
class RealDataEvaluationTest {

    private static final int[] CUTOFFS = {5, 10};
    private static final int LIKED = 4;
    private static final int SAMPLE_READERS = 200;
    private static final int MIN_RATINGS_PER_READER = 10;
    private static final int BASELINE_MIN_RATINGS = 30;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private RatingRepository realRatings;

    static boolean switchOn() {
        return Files.exists(Path.of(System.getProperty("user.home"), ".run-real-eval"));
    }

    @Test
    void comparesRecommendationMethodsOnRealRatings() {
        Random random = new Random(42);
        List<Rating> all = realRatings.findAll();
        Map<Long, List<Rating>> byUser = new LinkedHashMap<>();
        for (Rating rating : all) {
            byUser.computeIfAbsent(rating.getUser().getId(), k -> new ArrayList<>()).add(rating);
        }

        // Pick readers to test; everybody else stays fully in the training data.
        List<Long> candidates = new ArrayList<>();
        for (Map.Entry<Long, List<Rating>> entry : byUser.entrySet()) {
            if (entry.getValue().size() >= MIN_RATINGS_PER_READER) {
                candidates.add(entry.getKey());
            }
        }
        Collections.sort(candidates);
        Collections.shuffle(candidates, random);
        Set<Long> sample = new HashSet<>(candidates.subList(0, Math.min(SAMPLE_READERS, candidates.size())));

        List<Rating> train = new ArrayList<>();
        Map<Long, Set<Long>> relevant = new LinkedHashMap<>();
        for (Map.Entry<Long, List<Rating>> entry : byUser.entrySet()) {
            if (!sample.contains(entry.getKey())) {
                train.addAll(entry.getValue());
                continue;
            }
            List<Rating> mine = new ArrayList<>(entry.getValue());
            Collections.shuffle(mine, random);
            int testSize = Math.max(1, (int) Math.round(mine.size() * 0.2));
            for (int i = 0; i < mine.size(); i++) {
                Rating rating = mine.get(i);
                if (i < testSize) {
                    if (rating.getScore() >= LIKED) {
                        relevant.computeIfAbsent(entry.getKey(), k -> new HashSet<>())
                                .add(rating.getBook().getId());
                    }
                } else {
                    train.add(rating);
                }
            }
        }
        Map<Long, List<Rating>> trainByUser = new HashMap<>();
        for (Rating rating : train) {
            trainByUser.computeIfAbsent(rating.getUser().getId(), k -> new ArrayList<>()).add(rating);
        }

        // The strategies read ratings through this stand-in, which only knows the training data.
        RatingRepository ratingRepository = mock(RatingRepository.class);
        when(ratingRepository.findAll()).thenReturn(train);
        when(ratingRepository.findByUserId(anyLong())).thenAnswer(invocation -> {
            Long userId = invocation.getArgument(0);
            return trainByUser.getOrDefault(userId, List.of());
        });

        ContentBasedStrategy content = new ContentBasedStrategy(ratingRepository, bookRepository);
        CollaborativeStrategy collaborative = new CollaborativeStrategy(ratingRepository, bookRepository);
        HybridStrategy hybrid = new HybridStrategy(content, collaborative, ratingRepository);
        List<Long> popular = popularityRanking(train);

        double[] baselineScores = evaluate(userId -> {
            Set<Long> rated = new HashSet<>();
            for (Rating rating : trainByUser.getOrDefault(userId, List.of())) {
                rated.add(rating.getBook().getId());
            }
            return popular.stream().filter(id -> !rated.contains(id)).limit(10).toList();
        }, relevant);
        double[] contentScores = evaluate(userId -> idsOf(content.recommend(userId, 10)), relevant);
        double[] collaborativeScores = evaluate(userId -> idsOf(collaborative.recommend(userId, 10)), relevant);
        double[] hybridScores = evaluate(userId -> idsOf(hybrid.recommend(userId, 10)), relevant);

        System.out.println();
        System.out.println("EVALUATION ON REAL RATINGS: " + byUser.size() + " readers, "
                + bookRepository.count() + " books, " + all.size() + " ratings in total, "
                + train.size() + " used for training, 80/20 split for " + sample.size()
                + " sampled readers, " + relevant.size() + " readers evaluated");
        System.out.println(String.format(Locale.ROOT, "%-14s %8s %8s %8s %8s %8s %8s",
                "Method", "P@5", "R@5", "Hit@5", "P@10", "R@10", "Hit@10"));
        printRow("Popularity", baselineScores);
        printRow("Content", contentScores);
        printRow("Collaborative", collaborativeScores);
        printRow("Hybrid", hybridScores);
        System.out.println();

        assertTrue(relevant.size() > 50, "most sampled readers should have at least one liked hidden book");
    }

    /** Baseline: books with enough training ratings, best average first. */
    private static List<Long> popularityRanking(List<Rating> train) {
        Map<Long, int[]> stats = new HashMap<>();
        for (Rating rating : train) {
            int[] s = stats.computeIfAbsent(rating.getBook().getId(), k -> new int[2]);
            s[0] += rating.getScore();
            s[1]++;
        }
        return stats.entrySet().stream()
                .filter(e -> e.getValue()[1] >= BASELINE_MIN_RATINGS)
                .sorted((a, b) -> {
                    int byAverage = Double.compare((double) b.getValue()[0] / b.getValue()[1],
                            (double) a.getValue()[0] / a.getValue()[1]);
                    if (byAverage != 0) {
                        return byAverage;
                    }
                    int byCount = Integer.compare(b.getValue()[1], a.getValue()[1]);
                    return byCount != 0 ? byCount : Long.compare(a.getKey(), b.getKey());
                })
                .map(Map.Entry::getKey)
                .toList();
    }

    private static List<Long> idsOf(List<RecommendedBook> recommendations) {
        return recommendations.stream().map(RecommendedBook::id).toList();
    }

    /** Average precision, recall and hit rate over all readers, for each cutoff: [P@5, R@5, Hit@5, P@10, ...]. */
    private static double[] evaluate(Function<Long, List<Long>> recommender, Map<Long, Set<Long>> relevant) {
        double[] sums = new double[CUTOFFS.length * 3];
        for (Map.Entry<Long, Set<Long>> entry : relevant.entrySet()) {
            List<Long> recommended = recommender.apply(entry.getKey());
            for (int c = 0; c < CUTOFFS.length; c++) {
                int k = CUTOFFS[c];
                int hits = 0;
                for (int i = 0; i < Math.min(k, recommended.size()); i++) {
                    if (entry.getValue().contains(recommended.get(i))) {
                        hits++;
                    }
                }
                sums[c * 3] += (double) hits / k;
                sums[c * 3 + 1] += (double) hits / entry.getValue().size();
                sums[c * 3 + 2] += hits > 0 ? 1 : 0;
            }
        }
        for (int i = 0; i < sums.length; i++) {
            sums[i] /= relevant.size();
        }
        return sums;
    }

    private static void printRow(String name, double[] s) {
        System.out.println(String.format(Locale.ROOT, "%-14s %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f",
                name, s[0], s[1], s[2], s[3], s[4], s[5]));
    }
}
