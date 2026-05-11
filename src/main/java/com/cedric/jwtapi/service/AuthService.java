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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException(
                    "Le nom d'utilisateur '" + request.username() + "' est déjà utilisé");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(
                    "L'adresse email '" + request.email() + "' est déjà utilisée");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(Objects.requireNonNull(passwordEncoder.encode(request.password())))
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);

        return buildResponse(user);
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        User user = userRepository.findByUsername(request.username()).orElseThrow();
        return buildResponse(user);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String username = jwtService.extractUsername(request.refreshToken());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur introuvable : " + username));

        if (!jwtService.isTokenValid(request.refreshToken(), user)) {
            throw new BadCredentialsException("Refresh token invalide ou expiré");
        }

        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken, jwtService.getJwtExpiration());
    }
}
