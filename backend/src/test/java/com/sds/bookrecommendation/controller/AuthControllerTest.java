
package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.dto.AuthResponse;
import com.sds.bookrecommendation.security.JwtService;
import com.sds.bookrecommendation.repository.UserRepository;
import com.sds.bookrecommendation.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void register_validRequest_returnsCreated() throws Exception {

        when(authService.register(any()))
                .thenReturn(new AuthResponse(
                        1L, "Charu", "charu@gmail.com",
                        "register-token", "Bearer"
                ));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Charu",
                                    "email": "charu@gmail.com",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.name").value("Charu"))
                .andExpect(jsonPath("$.email").value("charu@gmail.com"))
                .andExpect(jsonPath("$.token").value("register-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        verify(authService).register(any());
    }

    @Test
    void register_invalidRequest_returnsBadRequest() throws Exception {

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "email": "invalid-email",
                                    "password": "123"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void login_validRequest_returnsOk() throws Exception {

        when(authService.login(any()))
                .thenReturn(new AuthResponse(
                        1L, "Charu", "charu@gmail.com",
                        "login-token", "Bearer"
                ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "charu@gmail.com",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("charu@gmail.com"))
                .andExpect(jsonPath("$.token").value("login-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        verify(authService).login(any());
    }

    @Test
    void login_invalidRequest_returnsBadRequest() throws Exception {

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "invalid-email",
                                    "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }
}
