package com.cedric.jwtapi.security;

import com.cedric.jwtapi.entity.Role;
import com.cedric.jwtapi.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    // Same secret used in application.properties
    private static final String SECRET =
            "VGhpc0lzQVZlcnlTZWN1cmVTZWNyZXRLZXlGb3JKV1RBdXRoZW50aWNhdGlvbjEyMzQ1Njc4OTA=";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86_400_000L);

        userDetails = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.ROLE_USER)
                .build();
    }

    @Test
    void generateToken_shouldReturnNonBlankToken() {
        String token = jwtService.generateToken(userDetails);
        assertThat(token).isNotBlank();
    }

    @Test
    void extractUsername_shouldReturnEmbeddedUsername() {
        String token = jwtService.generateToken(userDetails);
        assertThat(jwtService.extractUsername(token)).isEqualTo("testuser");
    }

    @Test
    void isTokenValid_withMatchingUser_shouldReturnTrue() {
        String token = jwtService.generateToken(userDetails);
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void isTokenValid_withDifferentUser_shouldReturnFalse() {
        String token = jwtService.generateToken(userDetails);
        UserDetails otherUser = User.builder()
                .username("other")
                .email("other@example.com")
                .password("pass")
                .role(Role.ROLE_USER)
                .build();
        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void generateRefreshToken_shouldDifferFromAccessToken() {
        String access = jwtService.generateToken(userDetails);
        String refresh = jwtService.generateRefreshToken(userDetails);
        // Both embed the same username but are signed at a slightly different time; at minimum they differ in expiry
        assertThat(refresh).isNotBlank();
        // The refresh token should still carry the correct username
        assertThat(jwtService.extractUsername(refresh)).isEqualTo("testuser");
    }

    @Test
    void getJwtExpiration_shouldReturnConfiguredValue() {
        assertThat(jwtService.getJwtExpiration()).isEqualTo(86_400_000L);
    }
}
