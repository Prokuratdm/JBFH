package com.par.jbfh.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "this-is-a-test-secret-key-that-is-long-enough-for-hs256",
                3600000L // 1 hour
        );
    }

    @Test
    void shouldGenerateValidToken() {
        UUID userId = UUID.randomUUID();
        List<String> roles = List.of("ROLE_ADMIN", "ROLE_COACH");

        String token = jwtService.generateToken("testuser", userId, roles);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void shouldExtractUsernameFromToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken("testuser", userId, List.of("ROLE_ADMIN"));

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void shouldExtractUserIdFromToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken("testuser", userId, List.of("ROLE_ADMIN"));

        UUID extractedUserId = jwtService.extractUserId(token);

        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    void shouldExtractRolesFromToken() {
        List<String> roles = List.of("ROLE_ADMIN", "ROLE_COACH");
        String token = jwtService.generateToken("testuser", UUID.randomUUID(), roles);

        List<String> extractedRoles = jwtService.extractRoles(token);

        assertThat(extractedRoles).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_COACH");
    }

    @Test
    void shouldReturnTrueForValidToken() {
        String token = jwtService.generateToken("testuser", UUID.randomUUID(), List.of("ROLE_ADMIN"));

        boolean valid = jwtService.isTokenValid(token);

        assertThat(valid).isTrue();
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        boolean valid = jwtService.isTokenValid("invalid.token.string");

        assertThat(valid).isFalse();
    }

    @Test
    void shouldReturnFalseForEmptyToken() {
        boolean valid = jwtService.isTokenValid("");

        assertThat(valid).isFalse();
    }

    @Test
    void shouldReturnFalseForNullToken() {
        boolean valid = jwtService.isTokenValid(null);

        assertThat(valid).isFalse();
    }
}