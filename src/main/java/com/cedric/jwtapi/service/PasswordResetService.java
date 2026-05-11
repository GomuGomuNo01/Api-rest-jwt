package com.cedric.jwtapi.service;

import com.cedric.jwtapi.entity.User;
import com.cedric.jwtapi.exception.InvalidTokenException;
import com.cedric.jwtapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final int TOKEN_EXPIRY_MINUTES = 15;

    /**
     * Génère un token de réinitialisation et l'envoie par email.
     * Ne révèle jamais si l'email existe ou non (prévention d'énumération).
     */
    @Transactional
    public void requestPasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.debug("Demande de reset pour email inconnu : {}", email);
            return;
        }

        User user = userOpt.get();
        String token = UUID.randomUUID().toString();

        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES));
        userRepository.save(user);

        emailService.sendPasswordResetEmail(email, token);
    }

    /**
     * Valide le token et applique le nouveau mot de passe.
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token de réinitialisation invalide"));

        if (user.getResetTokenExpiry() == null ||
                user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            userRepository.save(user);
            throw new InvalidTokenException("Token de réinitialisation expiré");
        }

        user.setPassword(Objects.requireNonNull(passwordEncoder.encode(newPassword)));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        log.info("Mot de passe réinitialisé pour l'utilisateur {}", user.getUsername());
    }
}
