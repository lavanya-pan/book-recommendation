
package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.repository.UserRepository;
import com.sds.bookrecommendation.service.JWTService;
import com.sds.bookrecommendation.service.UserProfileService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserProfileService userProfileService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        User principal = new User("Charu", "charu@gmail.com");
        principal.setId(1L);
        principal.setRole("USER");

        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        principal,
                        null,
                        java.util.Collections.emptyList()
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMyProfile_returnsUserProfile() throws Exception {
        User user = new User("Charu", "charu@gmail.com");
        user.setId(1L);
        user.setReaderType("FICTION");
        user.setTheme("DARK");
        user.setRole("USER");

        when(userProfileService.getProfile(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.name").value("Charu"))
                .andExpect(jsonPath("$.email").value("charu@gmail.com"))
                .andExpect(jsonPath("$.readerType").value("FICTION"))
                .andExpect(jsonPath("$.theme").value("DARK"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userProfileService).getProfile(1L);
    }

    @Test
    void updateTheme_validTheme_returnsUpdatedTheme() throws Exception {
        User user = new User("Charu", "charu@gmail.com");
        user.setId(1L);
        user.setTheme("DARK");

        when(userProfileService.updateTheme(1L, "DARK"))
                .thenReturn(user);

        mockMvc.perform(put("/api/users/me/theme")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "theme": "DARK"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.theme").value("DARK"));

        verify(userProfileService).updateTheme(1L, "DARK");
    }

    @Test
    void updateTheme_invalidTheme_returnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/users/me/theme")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "theme": "BLUE"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userProfileService);
    }

    @Test
    void updateReaderType_validValue_returnsUpdatedReaderType()
            throws Exception {
        User user = new User("Charu", "charu@gmail.com");
        user.setId(1L);
        user.setReaderType("FICTION");

        when(userProfileService.updateReaderType(1L, "FICTION"))
                .thenReturn(user);

        mockMvc.perform(put("/api/users/me/reader-type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "readerType": "FICTION"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.readerType").value("FICTION"));

        verify(userProfileService).updateReaderType(1L, "FICTION");
    }

    @Test
    void protectedEndpoint_returns403WithoutToken() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateReaderType_blankValue_returnsBadRequest()
            throws Exception {
        mockMvc.perform(put("/api/users/me/reader-type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "readerType": " "
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userProfileService);
    }

    @Test
    void deleteMyAccount_returnsNoContent() throws Exception {
        doNothing().when(userProfileService).deleteAccount(1L);

        mockMvc.perform(delete("/api/users/me"))
                .andExpect(status().isNoContent());

        verify(userProfileService).deleteAccount(1L);
    }
}
