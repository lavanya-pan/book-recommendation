
package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.dto.AuthResponse;
import com.sds.bookrecommendation.dto.LoginRequest;
import com.sds.bookrecommendation.dto.RegisterRequest;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.repository.UserRepository;
import com.sds.bookrecommendation.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_success() {

        RegisterRequest request =
                new RegisterRequest("  Charu  ", " TEST@GMAIL.COM ", "password123");

        when(userRepository.existsByEmail("test@gmail.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(1L);
                    return user;
                });

        when(jwtService.generateToken(1L, "test@gmail.com"))
                .thenReturn("test-token");

        AuthResponse response = authService.register(request);

        assertEquals(1L, response.userId());
        assertEquals("Charu", response.name());
        assertEquals("test@gmail.com", response.email());
        assertEquals("test-token", response.token());
        assertEquals("Bearer", response.tokenType());

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_duplicateEmail_throwsConflict() {

        RegisterRequest request =
                new RegisterRequest("Charu", "test@gmail.com", "password123");

        when(userRepository.existsByEmail("test@gmail.com"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(request)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_success() {

        LoginRequest request =
                new LoginRequest(" TEST@GMAIL.COM ", "password123");

        User user = mock(User.class);

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(user.getPassword()).thenReturn("hashedPassword");
        when(passwordEncoder.matches("password123", "hashedPassword"))
                .thenReturn(true);

        when(user.getId()).thenReturn(1L);
        when(user.getName()).thenReturn("Charu");
        when(user.getEmail()).thenReturn("test@gmail.com");

        when(jwtService.generateToken(1L, "test@gmail.com"))
                .thenReturn("login-token");

        AuthResponse response = authService.login(request);

        assertEquals(1L, response.userId());
        assertEquals("Charu", response.name());
        assertEquals("test@gmail.com", response.email());
        assertEquals("login-token", response.token());
        assertEquals("Bearer", response.tokenType());
    }

    @Test
    void login_unknownEmail_throwsUnauthorized() {

        LoginRequest request =
                new LoginRequest("unknown@gmail.com", "password123");

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request)
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {

        LoginRequest request =
                new LoginRequest("test@gmail.com", "wrongpassword");

        User user = mock(User.class);

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(user.getPassword()).thenReturn("hashedPassword");

        when(passwordEncoder.matches(
                "wrongpassword", "hashedPassword"))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request)
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );
    }
}
