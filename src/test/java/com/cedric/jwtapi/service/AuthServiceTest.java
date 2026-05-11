package com.cedric.jwtapi.service;

import com.cedric.jwtapi.dto.AuthRequest;
import com.cedric.jwtapi.dto.AuthResponse;
import com.cedric.jwtapi.dto.RefreshTokenRequest;
import com.cedric.jwtapi.dto.RegisterRequest;
import com.cedric.jwtapi.entity.Role;
import com.cedric.jwtapi.entity.User;
import com.cedric.jwtapi.exception.UserAlreadyExistsException;
import com.cedric.jwtapi.repository.UserRepository;
import com.cedric.jwtapi.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private final User alice = User.builder()
            .id(1L).username("alice").email("alice@example.com")
            .password("encodedPass").role(Role.ROLE_USER).build();

    // ── register ──────────────────────────────────────────────────────────────

    @Test
    void register_withNewUser_shouldReturnTokenPair() {
        RegisterRequest req = new RegisterRequest("alice", "alice@example.com", "password123");
        given(userRepository.existsByUsername("alice")).willReturn(false);
        given(userRepository.existsByEmail("alice@example.com")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("encodedPass");
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));
        given(jwtService.generateToken(any())).willReturn("access");
        given(jwtService.generateRefreshToken(any())).willReturn("refresh");
        given(jwtService.getJwtExpiration()).willReturn(86_400_000L);

        AuthResponse response = authService.register(req);

        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_withExistingUsername_shouldThrow() {
        RegisterRequest req = new RegisterRequest("alice", "alice@example.com", "password123");
        given(userRepository.existsByUsername("alice")).willReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("alice");
    }

    @Test
    void register_withExistingEmail_shouldThrow() {
        RegisterRequest req = new RegisterRequest("alice", "alice@example.com", "password123");
        given(userRepository.existsByUsername("alice")).willReturn(false);
        given(userRepository.existsByEmail("alice@example.com")).willReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("alice@example.com");
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void login_withValidCredentials_shouldReturnTokenPair() {
        AuthRequest req = new AuthRequest("alice", "password123");
        given(userRepository.findByUsername("alice")).willReturn(Optional.of(alice));
        given(jwtService.generateToken(alice)).willReturn("access");
        given(jwtService.generateRefreshToken(alice)).willReturn("refresh");
        given(jwtService.getJwtExpiration()).willReturn(86_400_000L);

        AuthResponse response = authService.login(req);

        assertThat(response.accessToken()).isEqualTo("access");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_withBadCredentials_shouldPropagate() {
        AuthRequest req = new AuthRequest("alice", "wrong");
        given(authenticationManager.authenticate(any()))
                .willThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
    }

    // ── refreshToken ──────────────────────────────────────────────────────────

    @Test
    void refreshToken_withValidToken_shouldReturnNewPair() {
        RefreshTokenRequest req = new RefreshTokenRequest("valid-refresh");
        given(jwtService.extractUsername("valid-refresh")).willReturn("alice");
        given(userRepository.findByUsername("alice")).willReturn(Optional.of(alice));
        given(jwtService.isTokenValid("valid-refresh", alice)).willReturn(true);
        given(jwtService.generateToken(alice)).willReturn("new-access");
        given(jwtService.generateRefreshToken(alice)).willReturn("new-refresh");
        given(jwtService.getJwtExpiration()).willReturn(86_400_000L);

        AuthResponse response = authService.refreshToken(req);

        assertThat(response.accessToken()).isEqualTo("new-access");
        assertThat(response.refreshToken()).isEqualTo("new-refresh");
    }

    @Test
    void refreshToken_withInvalidToken_shouldThrow() {
        RefreshTokenRequest req = new RefreshTokenRequest("expired-refresh");
        given(jwtService.extractUsername("expired-refresh")).willReturn("alice");
        given(userRepository.findByUsername("alice")).willReturn(Optional.of(alice));
        given(jwtService.isTokenValid("expired-refresh", alice)).willReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(req))
                .isInstanceOf(BadCredentialsException.class);
    }
}
