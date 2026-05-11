package com.cedric.jwtapi.controller;

import com.cedric.jwtapi.dto.AuthRequest;
import com.cedric.jwtapi.dto.AuthResponse;
import com.cedric.jwtapi.dto.RegisterRequest;
import com.cedric.jwtapi.exception.GlobalExceptionHandler;
import com.cedric.jwtapi.exception.UserAlreadyExistsException;
import com.cedric.jwtapi.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AuthResponse fakeResponse =
            new AuthResponse("access-token", "refresh-token", "Bearer", 86_400_000L);

    @BeforeEach
    void setUp() {
        // Standalone setup : aucun Spring context nécessaire.
        // Hibernate Validator (spring-boot-starter-validation) est sur le classpath
        // → @Valid est traité automatiquement par Spring MVC.
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void register_withValidData_shouldReturn201AndTokens() throws Exception {
        RegisterRequest request = new RegisterRequest("alice", "alice@example.com", "password123");
        given(authService.register(any())).willReturn(fakeResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    void register_withInvalidData_shouldReturn400WithFieldErrors() throws Exception {
        // username trop court, email invalide, password trop court → 3 violations @Valid
        RegisterRequest request = new RegisterRequest("a", "not-an-email", "short");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void register_withExistingUsername_shouldReturn409() throws Exception {
        RegisterRequest request = new RegisterRequest("alice", "alice@example.com", "password123");
        given(authService.register(any()))
                .willThrow(new UserAlreadyExistsException("Username taken"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_withValidCredentials_shouldReturn200AndTokens() throws Exception {
        AuthRequest request = new AuthRequest("alice", "password123");
        given(authService.login(any())).willReturn(fakeResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void login_withBadCredentials_shouldReturn401() throws Exception {
        AuthRequest request = new AuthRequest("alice", "wrongpass");
        given(authService.login(any()))
                .willThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withMissingBody_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
