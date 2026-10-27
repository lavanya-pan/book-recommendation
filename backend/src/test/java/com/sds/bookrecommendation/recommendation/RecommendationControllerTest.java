package com.sds.bookrecommendation.recommendation;

import com.sds.bookrecommendation.exception.BookNotFoundException;
import com.sds.bookrecommendation.exception.GlobalExceptionHandler;
import com.sds.bookrecommendation.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RecommendationControllerTest {

    private static final RecommendedBook BOOK =
            new RecommendedBook(7L, "Some Book", "Some Author", "Fantasy", 0.9, "because");

    private RecommendationService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(RecommendationService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new RecommendationController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returnsRecommendationsAsJson() throws Exception {
        when(service.recommend(1L, null, null)).thenReturn(List.of(BOOK));

        mockMvc.perform(get("/api/users/1/recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].title").value("Some Book"))
                .andExpect(jsonPath("$[0].score").value(0.9))
                .andExpect(jsonPath("$[0].reason").value("because"));
    }

    @Test
    void passesTypeAndLimitToService() throws Exception {
        when(service.recommend(1L, "genre", 5)).thenReturn(List.of(BOOK));

        mockMvc.perform(get("/api/users/1/recommendations").param("type", "genre").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].genre").value("Fantasy"));
    }

    @Test
    void unknownTypeReturns400() throws Exception {
        when(service.recommend(1L, "magic", null))
                .thenThrow(new IllegalArgumentException("Unknown recommendation type \"magic\""));

        mockMvc.perform(get("/api/users/1/recommendations").param("type", "magic"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("magic")));
    }

    @Test
    void unknownUserReturns404() throws Exception {
        when(service.recommend(99L, null, null)).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(get("/api/users/99/recommendations"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("99")));
    }

    @Test
    void nonNumericLimitReturns400() throws Exception {
        mockMvc.perform(get("/api/users/1/recommendations").param("limit", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("limit")));
    }

    @Test
    void similarBooksReturnsJson() throws Exception {
        when(service.similarBooks(3L, 6)).thenReturn(List.of(BOOK));

        mockMvc.perform(get("/api/books/3/similar").param("limit", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Some Book"));
    }

    @Test
    void similarBooksForUnknownBookReturns404() throws Exception {
        when(service.similarBooks(99L, null)).thenThrow(new BookNotFoundException(99L));

        mockMvc.perform(get("/api/books/99/similar"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listsRecommendationTypes() throws Exception {
        when(service.availableTypes()).thenReturn(List.of("content", "genre", "top-rated"));

        mockMvc.perform(get("/api/recommendation-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("content"))
                .andExpect(jsonPath("$[2]").value("top-rated"));
    }
}