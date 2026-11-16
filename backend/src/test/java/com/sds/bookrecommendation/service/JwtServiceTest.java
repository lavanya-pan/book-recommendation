
package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JWTService jwtService;
    private User user;

    private static final String SECRET =
            Base64.getEncoder().encodeToString(
                    "01234567890123456789012345678901"
                            .getBytes(StandardCharsets.UTF_8)
            );

    @BeforeEach
    void setUp() {
        jwtService = new JWTService(SECRET, 86400000L);

        user = new User("Charu", "charu@gmail.com");
        user.setId(1L);
    }

    @Test
    void generateToken_returnsValidToken() {
        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void extractUserId_returnsCorrectUserId() {
        String token = jwtService.generateToken(user);

        assertEquals(1L, jwtService.extractUserId(token));
    }

    @Test
    void invalidToken_returnsFalse() {
        assertFalse(jwtService.isTokenValid("invalid-token", user));
        assertFalse(jwtService.isTokenValid("", user));
        assertFalse(jwtService.isTokenValid(null, user));
    }

    @Test
    void expiredToken_returnsFalse() {
        JWTService expiredJwtService = new JWTService(SECRET, -1000L);

        String token = expiredJwtService.generateToken(user);

        assertFalse(expiredJwtService.isTokenValid(token, user));
    }

    @Test
    void tokenSignedWithDifferentSecret_returnsFalse() {
        String token = jwtService.generateToken(user);

        String anotherSecret =
                Base64.getEncoder().encodeToString(
                        "98765432109876543210987654321098"
                                .getBytes(StandardCharsets.UTF_8)
                );

        JWTService anotherJwtService =
                new JWTService(anotherSecret, 86400000L);

        assertFalse(anotherJwtService.isTokenValid(token, user));
    }

    @Test
    void tokenForDifferentUser_returnsFalse() {
        String token = jwtService.generateToken(user);

        User anotherUser = new User("Another", "another@gmail.com");
        anotherUser.setId(2L);

        assertFalse(jwtService.isTokenValid(token, anotherUser));
    }
}
