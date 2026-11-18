package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.dto.ReviewRequest;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.repository.UserRepository;
import com.sds.bookrecommendation.service.JWTService;
import com.sds.bookrecommendation.service.ReviewService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private UserRepository userRepository;

    // Logs in a fake user with id 1 before each test.
    @BeforeEach
    void setUp() {
        User principal = new User("Charu", "charu@gmail.com");
        principal.setId(1L);
        principal.setRole("USER");

        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        principal, null, java.util.Collections.emptyList()));
    }

    // Clears the fake login after each test.
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // A userId sent in the body must be replaced by the logged-in user's id.
    @Test
    void createReview_usesLoggedInUser_ignoresUserIdInBody() throws Exception {
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": 999,
                                    "bookId": 3,
                                    "text": "Great book"
                                }
                                """))
                .andExpect(status().isCreated());

        ArgumentCaptor<ReviewRequest> captor =
                ArgumentCaptor.forClass(ReviewRequest.class);
        verify(reviewService).createReview(captor.capture());

        assertEquals(1L, captor.getValue().userId());
        assertEquals(3L, captor.getValue().bookId());
        assertEquals("Great book", captor.getValue().text());
    }

    // The client does not have to send a userId at all.
    @Test
    void createReview_withoutUserId_isAccepted() throws Exception {
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "bookId": 3,
                                    "text": "Great book"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    // An empty review text is rejected and nothing is saved.
    @Test
    void createReview_blankText_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "bookId": 3,
                                    "text": " "
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewService);
    }

    // Reading the reviews of a book works for anyone.
    @Test
    void getReviewsByBook_returnsList() throws Exception {
        when(reviewService.getReviewsByBook(3L)).thenReturn(List.of());

        mockMvc.perform(get("/api/books/3/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(reviewService).getReviewsByBook(3L);
    }

    // Without a logged-in user, creating a review is refused and nothing is saved.
    @Test
    void createReview_withoutLogin_isForbidden() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "bookId": 3,
                                    "text": "Great book"
                                }
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(reviewService);
    }
}
