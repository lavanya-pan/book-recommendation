package com.sds.bookrecommendation.security;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET =
            Base64.getEncoder().encodeToString(
                    "01234567890123456789012345678901"
                            .getBytes(StandardCharsets.UTF_8)
            );

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(
                jwtService, "secret", SECRET
        );

        ReflectionTestUtils.setField(
                jwtService, "expiration", 86400000L
        );
    }

    @Test
    void generateToken_returnsValidToken() {

        String token = jwtService.generateToken(
                1L, "charu@gmail.com"
        );

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void extractEmail_returnsCorrectEmail() {

        String token = jwtService.generateToken(
                1L, "charu@gmail.com"
        );

        assertEquals(
                "charu@gmail.com",
                jwtService.extractEmail(token)
        );
    }

    @Test
    void extractUserId_returnsCorrectUserId() {

        String token = jwtService.generateToken(
                25L, "charu@gmail.com"
        );

        assertEquals(
                25L,
                jwtService.extractUserId(token)
        );
    }

    @Test
    void invalidToken_returnsFalse() {

        assertFalse(jwtService.isTokenValid("invalid-token"));
        assertFalse(jwtService.isTokenValid(""));
        assertFalse(jwtService.isTokenValid(null));
    }

    @Test
    void expiredToken_returnsFalse() {

        ReflectionTestUtils.setField(
                jwtService, "expiration", -1000L
        );

        String token = jwtService.generateToken(
                1L, "charu@gmail.com"
        );

        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void tokenSignedWithDifferentSecret_returnsFalse() {

        String token = jwtService.generateToken(
                1L, "charu@gmail.com"
        );

        JwtService anotherJwtService = new JwtService();

        String anotherSecret =
                Base64.getEncoder().encodeToString(
                        "98765432109876543210987654321098"
                                .getBytes(StandardCharsets.UTF_8)
                );

        ReflectionTestUtils.setField(
                anotherJwtService, "secret", anotherSecret
        );

        assertFalse(anotherJwtService.isTokenValid(token));
    }
}
