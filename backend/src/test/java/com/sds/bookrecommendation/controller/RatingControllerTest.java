
package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.dto.RatingResponse;
import com.sds.bookrecommendation.exception.DuplicateRatingException;
import com.sds.bookrecommendation.exception.RatingNotFoundException;
import com.sds.bookrecommendation.exception.UserNotFoundException;
import com.sds.bookrecommendation.repository.UserRepository;
import com.sds.bookrecommendation.service.RatingService;
import com.sds.bookrecommendation.service.JWTService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RatingController.class)
@AutoConfigureMockMvc(addFilters = false)
class RatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RatingService ratingService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private UserRepository userRepository;

    private static RatingResponse response(long id, int score) {
        return new RatingResponse(
                id, 1L, 1L, "Test Book", score,
                LocalDateTime.of(2026, 11, 14, 10, 0)
        );
    }

    @Test
    void createRating_returns201() throws Exception {
        when(ratingService.rateBook(any())).thenReturn(response(1, 4));

        mockMvc.perform(post("/api/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"bookId\":1,\"score\":4}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.score").value(4));
    }

    @Test
    void createRating_scoreSix_returns400() throws Exception {
        mockMvc.perform(post("/api/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"bookId\":1,\"score\":6}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRating_missingUserId_returns400() throws Exception {
        mockMvc.perform(post("/api/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1,\"score\":4}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRating_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{bad"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRating_duplicate_returns409() throws Exception {
        when(ratingService.rateBook(any()))
                .thenThrow(new DuplicateRatingException(1L, 1L));

        mockMvc.perform(post("/api/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"bookId\":1,\"score\":5}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void updateRating_returns200() throws Exception {
        when(ratingService.updateRating(eq(10L), any()))
                .thenReturn(response(10, 2));

        mockMvc.perform(put("/api/ratings/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(2));
    }

    @Test
    void updateRating_unknownId_returns404() throws Exception {
        when(ratingService.updateRating(eq(99L), any()))
                .thenThrow(new RatingNotFoundException(99L));

        mockMvc.perform(put("/api/ratings/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":3}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deleteRating_returns204() throws Exception {
        mockMvc.perform(delete("/api/ratings/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void listUserRatings_returns200() throws Exception {
        when(ratingService.getRatingsByUser(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of(response(1, 4))));

        mockMvc.perform(get("/api/users/1/ratings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].score").value(4));
    }

    @Test
    void listUserRatings_unknownUser_returns404() throws Exception {
        when(ratingService.getRatingsByUser(eq(99L), any()))
                .thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(get("/api/users/99/ratings"))
                .andExpect(status().isNotFound());
    }
}
