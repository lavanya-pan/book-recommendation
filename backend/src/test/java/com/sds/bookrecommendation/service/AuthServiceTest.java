
package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.repository.UserRepository;
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

    @InjectMocks
    private AuthService authService;

    @Test
    void register_success() {

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

        User savedUser = authService.register(
                "  Charu  ",
                " TEST@GMAIL.COM ",
                "password123"
        );

        assertEquals(1L, savedUser.getId());
        assertEquals("Charu", savedUser.getName());
        assertEquals("test@gmail.com", savedUser.getEmail());
        assertEquals("hashedPassword", savedUser.getPasswordHash());

        verify(userRepository).existsByEmail("test@gmail.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsConflict() {

        when(userRepository.existsByEmail("test@gmail.com"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(
                        "Charu",
                        "test@gmail.com",
                        "password123"
                )
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shortPassword_throwsBadRequest() {

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(
                        "Charu",
                        "test@gmail.com",
                        "123"
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void login_success() {

        User user = new User("Charu", "test@gmail.com");
        user.setId(1L);
        user.setPasswordHash("hashedPassword");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password123", "hashedPassword"))
                .thenReturn(true);

        User loggedInUser = authService.login(
                " TEST@GMAIL.COM ",
                "password123"
        );

        assertEquals(1L, loggedInUser.getId());
        assertEquals("Charu", loggedInUser.getName());
        assertEquals("test@gmail.com", loggedInUser.getEmail());

        verify(passwordEncoder).matches("password123", "hashedPassword");
    }

    @Test
    void login_unknownEmail_throwsUnauthorized() {

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(
                        "unknown@gmail.com",
                        "password123"
                )
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {

        User user = new User("Charu", "test@gmail.com");
        user.setPasswordHash("hashedPassword");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrongpassword", "hashedPassword"))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(
                        "test@gmail.com",
                        "wrongpassword"
                )
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void login_nullPasswordHash_throwsUnauthorized() {

        User user = new User("Charu", "test@gmail.com");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(
                        "test@gmail.com",
                        "password123"
                )
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());

        verifyNoInteractions(passwordEncoder);
    }
}
