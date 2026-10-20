package com.sds.bookrecommendation.recommendation;

import com.sds.bookrecommendation.entity.Rating;
import com.sds.bookrecommendation.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HybridStrategyTest {

    private ContentBasedStrategy content;
    private CollaborativeStrategy collaborative;
    private RatingRepository ratingRepository;
    private HybridStrategy strategy;

    @BeforeEach
    void setUp() {
        content = mock(ContentBasedStrategy.class);
        collaborative = mock(CollaborativeStrategy.class);
        ratingRepository = mock(RatingRepository.class);
        strategy = new HybridStrategy(content, collaborative, ratingRepository);

        // Content engine: book 1 is its best match (1.0), book 2 is half as good.
        when(content.recommend(eq(1L), anyInt())).thenReturn(List.of(
                book(1L, 1.0, "content reason"), book(2L, 0.5, "content reason")));
        // Collaborative engine: book 2 is its best match (4.0), book 3 is half as good.
        when(collaborative.recommend(eq(1L), anyInt())).thenReturn(List.of(
                book(2L, 4.0, "collaborative reason"), book(3L, 2.0, "collaborative reason")));
    }

    private static RecommendedBook book(long id, double score, String reason) {
        return new RecommendedBook(id, "Book " + id, "Author", "Genre", score, reason);
    }

    private void userHasRated(int count) {
        when(ratingRepository.findByUserId(1L))
                .thenReturn(Collections.nCopies(count, mock(Rating.class)));
    }

    @Test
    void typeIsHybrid() {
        assertEquals("hybrid", strategy.type());
    }

    @Test
    void contentWeightFallsAsTheUserRatesMoreBooks() {
        assertEquals(0.9, HybridStrategy.contentWeight(0));
        assertEquals(0.9, HybridStrategy.contentWeight(4));
        assertEquals(0.6, HybridStrategy.contentWeight(5));
        assertEquals(0.6, HybridStrategy.contentWeight(19));
        assertEquals(0.3, HybridStrategy.contentWeight(20));
        assertEquals(0.3, HybridStrategy.contentWeight(100));
    }

    @Test
    void newUserLeansOnContent() {
        userHasRated(2);

        List<RecommendedBook> result = strategy.recommend(1L, 5);

        assertEquals(List.of(1L, 2L, 3L), result.stream().map(RecommendedBook::id).toList());
        assertEquals(0.9, result.get(0).score(), 0.001);
        assertEquals(0.55, result.get(1).score(), 0.001);
        assertEquals(0.05, result.get(2).score(), 0.001);
    }

    @Test
    void experiencedUserLeansOnCollaborative() {
        userHasRated(25);

        List<RecommendedBook> result = strategy.recommend(1L, 5);

        assertEquals(List.of(2L, 3L, 1L), result.stream().map(RecommendedBook::id).toList());
        assertEquals(0.85, result.get(0).score(), 0.001);
        assertEquals(0.35, result.get(1).score(), 0.001);
        assertEquals(0.3, result.get(2).score(), 0.001);
    }

    @Test
    void reasonComesFromTheEngineThatContributedMore() {
        userHasRated(2);
        assertEquals("content reason", strategy.recommend(1L, 5).get(1).reason());

        userHasRated(25);
        assertEquals("collaborative reason", strategy.recommend(1L, 5).get(0).reason());
    }

    @Test
    void respectsTheLimit() {
        userHasRated(2);

        List<RecommendedBook> result = strategy.recommend(1L, 2);

        assertEquals(List.of(1L, 2L), result.stream().map(RecommendedBook::id).toList());
    }

    @Test
    void stillWorksWhenOnlyOneEngineFindsBooks() {
        userHasRated(2);
        when(content.recommend(eq(1L), anyInt())).thenReturn(List.of());

        List<RecommendedBook> result = strategy.recommend(1L, 5);

        assertEquals(List.of(2L, 3L), result.stream().map(RecommendedBook::id).toList());
        assertEquals(0.1, result.get(0).score(), 0.001);
    }

    @Test
    void returnsEmptyWhenNeitherEngineFindsAnything() {
        userHasRated(0);
        when(content.recommend(eq(1L), anyInt())).thenReturn(List.of());
        when(collaborative.recommend(eq(1L), anyInt())).thenReturn(List.of());

        assertTrue(strategy.recommend(1L, 5).isEmpty());
    }

    @Test
    void returnsEmptyForNullUserOrBadLimit() {
        assertTrue(strategy.recommend(null, 5).isEmpty());
        assertTrue(strategy.recommend(1L, 0).isEmpty());
    }
}