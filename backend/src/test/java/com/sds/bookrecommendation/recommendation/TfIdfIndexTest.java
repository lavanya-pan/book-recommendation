package com.sds.bookrecommendation.recommendation;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for TfIdfIndex using five tiny made-up books.
 */
class TfIdfIndexTest {

    /** Books 1, 2 and 5 are fantasy, 3 is a mystery, 4 is a romance. */
    private TfIdfIndex index() {
        return new TfIdfIndex(Map.of(
                1L, "dragon wizard magic kingdom fantasy",
                2L, "dragon wizard magic sword fantasy",
                3L, "detective murder mystery london",
                4L, "love romance wedding heart",
                5L, "wizard school magic fantasy"));
    }

    @Test
    void tokenizeDropsStopWordsAndShortWords() {
        assertEquals(List.of("dragon", "wizard"), TfIdfIndex.tokenize("The Dragon and a Wizard!"));
    }

    @Test
    void tokenizeHandlesNullText() {
        assertTrue(TfIdfIndex.tokenize(null).isEmpty());
    }

    @Test
    void similarToFindsOnlyBooksWithSharedWordsMostSimilarFirst() {
        List<TfIdfIndex.Match> matches = index().similarTo(1L, 10);

        // Book 2 shares 4 words with book 1, book 5 shares 3; the mystery and romance share none.
        assertEquals(List.of(2L, 5L), matches.stream().map(TfIdfIndex.Match::bookId).toList());
        assertTrue(matches.get(0).similarity() > matches.get(1).similarity());
    }

    @Test
    void similarToNeverReturnsTheBookItself() {
        List<TfIdfIndex.Match> matches = index().similarTo(1L, 10);

        assertTrue(matches.stream().noneMatch(m -> m.bookId() == 1L));
    }

    @Test
    void similarToRespectsTheLimit() {
        assertEquals(1, index().similarTo(1L, 1).size());
    }

    @Test
    void similarToUnknownBookReturnsNothing() {
        assertTrue(index().similarTo(99L, 5).isEmpty());
    }

    @Test
    void profileOfLikedBooksFindsOtherSimilarBooks() {
        TfIdfIndex index = index();
        Map<String, Double> profile = index.profile(Map.of(1L, 5.0, 2L, 4.0));

        List<TfIdfIndex.Match> matches = index.findSimilar(profile, Set.of(1L, 2L), 5);

        // Only book 5 is left that shares words with the liked fantasy books.
        assertEquals(List.of(5L), matches.stream().map(TfIdfIndex.Match::bookId).toList());
    }

    @Test
    void profileOfUnknownBooksIsEmptyAndFindsNothing() {
        TfIdfIndex index = index();
        Map<String, Double> profile = index.profile(Map.of(99L, 5.0));

        assertTrue(profile.isEmpty());
        assertTrue(index.findSimilar(profile, Set.of(), 5).isEmpty());
    }
}