package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.dto.ReadingHistoryRequest;
import com.sds.bookrecommendation.entity.ReadingStatus;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.repository.UserRepository;
import com.sds.bookrecommendation.service.JWTService;
import com.sds.bookrecommendation.service.ReadingHistoryService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReadingHistoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReadingHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReadingHistoryService readingHistoryService;

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

    // A userId sent in the URL must be ignored; the logged-in user's history is read.
    @Test
    void getHistory_usesLoggedInUser_ignoresUserIdParameter() throws Exception {
        when(readingHistoryService.getHistory(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/reading-history").param("userId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(readingHistoryService).getHistory(1L);
        verify(readingHistoryService, never()).getHistory(999L);
    }

    // A userId sent in the body must be replaced by the logged-in user's id.
    @Test
    void addOrUpdate_usesLoggedInUser_ignoresUserIdInBody() throws Exception {
        mockMvc.perform(post("/api/reading-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": 999,
                                    "bookId": 5,
                                    "status": "READING"
                                }
                                """))
                .andExpect(status().isCreated());

        ArgumentCaptor<ReadingHistoryRequest> captor =
                ArgumentCaptor.forClass(ReadingHistoryRequest.class);
        verify(readingHistoryService).addOrUpdate(captor.capture());

        assertEquals(1L, captor.getValue().getUserId());
        assertEquals(5L, captor.getValue().getBookId());
        assertEquals(ReadingStatus.READING, captor.getValue().getStatus());
    }

    // A request without a status is rejected and nothing is saved.
    @Test
    void addOrUpdate_missingStatus_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/reading-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "bookId": 5
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(readingHistoryService);
    }

    // Changing a status always applies to the logged-in user's own history.
    @Test
    void updateStatus_usesLoggedInUser() throws Exception {
        mockMvc.perform(put("/api/reading-history/5")
                        .param("userId", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "status": "FINISHED"
                                }
                                """))
                .andExpect(status().isOk());

        verify(readingHistoryService)
                .updateStatus(eq(1L), eq(5L), any());
    }

    // Without a logged-in user the endpoint is refused and the service is never called.
    @Test
    void getHistory_withoutLogin_isForbidden() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/reading-history"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(readingHistoryService);
    }
}
