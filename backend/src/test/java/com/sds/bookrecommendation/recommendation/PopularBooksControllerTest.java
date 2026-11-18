package com.sds.bookrecommendation.recommendation;

import com.sds.bookrecommendation.exception.GlobalExceptionHandler;
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

class PopularBooksControllerTest {

    private static final RecommendedBook BOOK =
            new RecommendedBook(7L, "Some Book", "Some Author", "Fantasy", 4.5, "Rated 4.5 out of 5 by 4 readers");

    private PopularBooksService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(PopularBooksService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new PopularBooksController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void topRatedReturnsJson() throws Exception {
        when(service.topRated(null)).thenReturn(List.of(BOOK));

        mockMvc.perform(get("/api/books/top-rated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Some Book"))
                .andExpect(jsonPath("$[0].reason").value("Rated 4.5 out of 5 by 4 readers"));
    }

    @Test
    void trendingPassesTheLimit() throws Exception {
        when(service.trending(3)).thenReturn(List.of(BOOK));

        mockMvc.perform(get("/api/books/trending").param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7));
    }

    @Test
    void nonNumericLimitReturns400() throws Exception {
        mockMvc.perform(get("/api/books/top-rated").param("limit", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("limit")));
    }
}