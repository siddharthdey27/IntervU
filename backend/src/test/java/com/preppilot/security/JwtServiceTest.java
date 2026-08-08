package com.preppilot.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "a_very_secret_key_for_jwt_testing_purposes_123456789");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMs", 3600000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpirationMs", 604800000L);
    }

    @Test
    void testAccessTokenGenerationAndValidation() {
        String userId = UUID.randomUUID().toString();
        String email = "test@example.com";

        String accessToken = jwtService.generateAccessToken(userId, email);

        assertNotNull(accessToken);
        assertTrue(jwtService.isTokenValid(accessToken));
        assertTrue(jwtService.isAccessToken(accessToken));
        assertFalse(jwtService.isRefreshToken(accessToken));
        assertEquals(userId, jwtService.extractUserId(accessToken));
        assertEquals(email, jwtService.extractEmail(accessToken));
    }

    @Test
    void testRefreshTokenGenerationAndValidation() {
        String userId = UUID.randomUUID().toString();
        String email = "test@example.com";

        String refreshToken = jwtService.generateRefreshToken(userId, email);

        assertNotNull(refreshToken);
        assertTrue(jwtService.isTokenValid(refreshToken));
        assertTrue(jwtService.isRefreshToken(refreshToken));
        assertFalse(jwtService.isAccessToken(refreshToken));
        assertEquals(userId, jwtService.extractUserId(refreshToken));
        assertEquals(email, jwtService.extractEmail(refreshToken));
    }
}
