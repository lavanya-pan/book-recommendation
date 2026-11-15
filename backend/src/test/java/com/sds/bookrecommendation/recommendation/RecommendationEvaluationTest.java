package com.sds.bookrecommendation.recommendation;

import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.Rating;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.RatingRepository;
import org.junit.jupiter.api.Test;

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
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Offline evaluation of the recommendation methods on SIMULATED readers.
 * Each reader's ratings are split 80/20. The strategies only see the 80%, and we
 * measure how many of the books the reader liked in the hidden 20% they recommend.
 * Run this test on its own and read the table it prints in the console.
 */
class RecommendationEvaluationTest {

    private static final int[] CUTOFFS = {5, 10};
    private static final int LIKED = 4;
    private static final int READERS = 90;
    private static final int BOOKS_PER_GENRE = 20;

    private static final String[] GENRES = {"Fantasy", "Mystery", "Romance"};
    private static final String[][] VOCAB = {
            {"dragon", "wizard", "magic", "kingdom", "sword", "quest", "elves", "castle"},
            {"detective", "murder", "clue", "suspect", "crime", "alibi", "inspector", "secret"},
            {"love", "wedding", "heart", "kiss", "romance", "passion", "marriage", "devotion"}};
    private static final String[] COMMON =
            {"journey", "world", "family", "story", "city", "night", "friend", "time"};

    @Test
    void comparesRecommendationMethods() {
        Random random = new Random(42);
        List<Book> books = makeBooks(random);
        List<Rating> allRatings = makeRatings(random, books);

        // Split every reader's ratings: about 20% are hidden for testing.
        Map<Long, List<Rating>> byUser = new LinkedHashMap<>();
        for (Rating rating : allRatings) {
            byUser.computeIfAbsent(rating.getUser().getId(), k -> new ArrayList<>()).add(rating);
        }
        List<Rating> train = new ArrayList<>();
        Map<Long, Set<Long>> relevant = new LinkedHashMap<>();
        for (Map.Entry<Long, List<Rating>> entry : byUser.entrySet()) {
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

        // The strategies read ratings and books through these stand-ins, which only know the training data.
        RatingRepository ratingRepository = mock(RatingRepository.class);
        when(ratingRepository.findAll()).thenReturn(train);
        when(ratingRepository.findByUserId(anyLong())).thenAnswer(invocation -> {
            Long userId = invocation.getArgument(0);
            return trainByUser.getOrDefault(userId, List.of());
        });
        BookRepository bookRepository = mock(BookRepository.class);
        when(bookRepository.count()).thenReturn((long) books.size());
        when(bookRepository.findAll()).thenReturn(books);
        when(bookRepository.findAllById(anyIterable())).thenAnswer(invocation -> {
            List<Book> found = new ArrayList<>();
            for (Object id : (Iterable<?>) invocation.getArgument(0)) {
                books.stream().filter(b -> b.getId().equals(id)).forEach(found::add);
            }
            return found;
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
        System.out.println("EVALUATION ON SIMULATED DATA: " + READERS + " readers, "
                + books.size() + " books, " + train.size() + " training ratings, 80/20 split per reader, "
                + relevant.size() + " readers evaluated");
        System.out.println(String.format(Locale.ROOT, "%-14s %8s %8s %8s %8s %8s %8s",
                "Method", "P@5", "R@5", "Hit@5", "P@10", "R@10", "Hit@10"));
        printRow("Popularity", baselineScores);
        printRow("Content", contentScores);
        printRow("Collaborative", collaborativeScores);
        printRow("Hybrid", hybridScores);
        System.out.println();

        assertTrue(relevant.size() > 50, "most readers should have at least one liked hidden book");
        for (double[] scores : List.of(baselineScores, contentScores, collaborativeScores, hybridScores)) {
            for (double score : scores) {
                assertTrue(score >= 0 && score <= 1, "every metric must be between 0 and 1");
            }
        }
    }

    /** Three genres of books; each description mixes genre words with a few common words. */
    private static List<Book> makeBooks(Random random) {
        List<Book> list = new ArrayList<>();
        long id = 1;
        for (int g = 0; g < GENRES.length; g++) {
            for (int i = 0; i < BOOKS_PER_GENRE; i++) {
                List<String> words = new ArrayList<>();
                for (int w = 0; w < 5; w++) {
                    words.add(VOCAB[g][random.nextInt(8)]);
                }
                if (random.nextInt(4) == 0) {
                    int other = (g + 1 + random.nextInt(2)) % 3;
                    words.add(VOCAB[other][random.nextInt(8)]);
                }
                for (int w = 0; w < 3; w++) {
                    words.add(COMMON[random.nextInt(COMMON.length)]);
                }
                Book book = new Book("Book " + id, "Author", GENRES[g], String.join(" ", words), 2000);
                book.setId(id);
                list.add(book);
                id++;
            }
        }
        return list;
    }

    /** Each reader has a favourite genre: 12 ratings there (mostly liked) and 4 elsewhere (mostly not). */
    private static List<Rating> makeRatings(Random random, List<Book> books) {
        List<Rating> list = new ArrayList<>();
        for (long userId = 1; userId <= READERS; userId++) {
            User user = new User("User " + userId, "user" + userId + "@example.com");
            user.setId(userId);
            String favourite = GENRES[(int) (userId % GENRES.length)];
            List<Book> own = new ArrayList<>();
            List<Book> other = new ArrayList<>();
            for (Book book : books) {
                if (book.getGenre().equals(favourite)) {
                    own.add(book);
                } else {
                    other.add(book);
                }
            }
            Collections.shuffle(own, random);
            Collections.shuffle(other, random);
            for (Book book : own.subList(0, 12)) {
                list.add(new Rating(user, book, ownGenreScore(random)));
            }
            for (Book book : other.subList(0, 4)) {
                list.add(new Rating(user, book, otherGenreScore(random)));
            }
        }
        return list;
    }

    private static int ownGenreScore(Random random) {
        int roll = random.nextInt(10);
        if (roll < 7) {
            return 4 + random.nextInt(2);
        }
        return roll < 9 ? 3 : 2;
    }

    private static int otherGenreScore(Random random) {
        return random.nextInt(100) < 15 ? 4 : 1 + random.nextInt(3);
    }

    /** Baseline: books with at least 3 ratings, best average first. */
    private static List<Long> popularityRanking(List<Rating> train) {
        Map<Long, int[]> stats = new HashMap<>();
        for (Rating rating : train) {
            int[] s = stats.computeIfAbsent(rating.getBook().getId(), k -> new int[2]);
            s[0] += rating.getScore();
            s[1]++;
        }
        return stats.entrySet().stream()
                .filter(e -> e.getValue()[1] >= 3)
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