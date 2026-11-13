package com.sds.bookrecommendation.recommendation;

import com.sds.bookrecommendation.exception.UserNotFoundException;
import com.sds.bookrecommendation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecommendationServiceTest {

    private static final RecommendedBook BOOK =
            new RecommendedBook(7L, "Some Book", "Some Author", "Fantasy", 0.9, "because");

    private RecommendationStrategy content;
    private RecommendationStrategy genre;
    private RecommendationStrategy topRated;
    private ContentBasedStrategy contentBased;
    private UserRepository userRepository;
    private RecommendationService service;

    @BeforeEach
    void setUp() {
        content = strategyNamed("content");
        genre = strategyNamed("genre");
        topRated = strategyNamed("top-rated");
        contentBased = mock(ContentBasedStrategy.class);
        userRepository = mock(UserRepository.class);
        when(userRepository.existsById(1L)).thenReturn(true);

        service = new RecommendationService(
                List.of(content, genre, topRated), contentBased, userRepository);
    }

    private static RecommendationStrategy strategyNamed(String type) {
        RecommendationStrategy strategy = mock(RecommendationStrategy.class);
        when(strategy.type()).thenReturn(type);
        return strategy;
    }

    @Test
    void listsAvailableTypesAlphabetically() {
        assertEquals(List.of("content", "genre", "top-rated"), service.availableTypes());
    }

    @Test
    void usesRequestedStrategy() {
        when(genre.recommend(1L, 5)).thenReturn(List.of(BOOK));

        assertEquals(List.of(BOOK), service.recommend(1L, "genre", 5));
        verify(content, never()).recommend(1L, 5);
    }

    @Test
    void typeIsCaseInsensitiveAndBlankMeansContent() {
        when(genre.recommend(1L, 10)).thenReturn(List.of(BOOK));
        when(content.recommend(1L, 10)).thenReturn(List.of(BOOK));

        assertEquals(List.of(BOOK), service.recommend(1L, "  GENRE ", null));
        assertEquals(List.of(BOOK), service.recommend(1L, null, null));
        assertEquals(List.of(BOOK), service.recommend(1L, "", null));
    }

    @Test
    void unknownTypeIsRejectedAndListsValidTypes() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.recommend(1L, "magic", 5));

        assertTrue(error.getMessage().contains("magic"));
        assertTrue(error.getMessage().contains("content, genre, top-rated"));
    }

    @Test
    void unknownUserIsRejected() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> service.recommend(99L, "content", 5));
        assertThrows(UserNotFoundException.class, () -> service.recommend(null, "content", 5));
    }

    @Test
    void fallsBackToTopRatedWhenStrategyHasNothing() {
        when(content.recommend(1L, 5)).thenReturn(List.of());
        when(topRated.recommend(1L, 5)).thenReturn(List.of(BOOK));

        assertEquals(List.of(BOOK), service.recommend(1L, "content", 5));
    }

    @Test
    void topRatedIsNotRetriedWhenEmpty() {
        when(topRated.recommend(1L, 5)).thenReturn(List.of());

        assertTrue(service.recommend(1L, "top-rated", 5).isEmpty());
    }

    @Test
    void limitDefaultsToTenAndIsCappedAtFifty() {
        when(content.recommend(1L, 10)).thenReturn(List.of(BOOK));
        when(content.recommend(1L, 50)).thenReturn(List.of(BOOK));

        service.recommend(1L, "content", null);
        service.recommend(1L, "content", 500);

        verify(content).recommend(1L, 10);
        verify(content).recommend(1L, 50);
    }

    @Test
    void limitBelowOneIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.recommend(1L, "content", 0));
        assertThrows(IllegalArgumentException.class, () -> service.similarBooks(3L, -1));
    }

    @Test
    void similarBooksDelegatesWithDefaultLimit() {
        when(contentBased.similarBooks(3L, 10)).thenReturn(List.of(BOOK));

        assertEquals(List.of(BOOK), service.similarBooks(3L, null));
    }
}