package com.cedric.jwtapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@jwt-api.com}")
    private String from;

    @Value("${app.reset-url:http://localhost:8080}")
    private String resetBaseUrl;

    public void sendPasswordResetEmail(String to, String token) {
        String resetLink = resetBaseUrl + "/api/v1/auth/reset-password?token=" + token;

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("Réinitialisation de votre mot de passe");
            message.setText("""
                    Bonjour,

                    Vous avez demandé la réinitialisation de votre mot de passe.

                    Utilisez le token ci-dessous dans votre requête POST /api/v1/auth/reset-password :

                    Token : %s

                    Ce token est valable 15 minutes. Ignorez cet email si vous n'êtes pas à l'origine de cette demande.

                    Cordialement,
                    L'équipe JWT API
                    """.formatted(token));
            mailSender.send(message);
            log.info("Email de réinitialisation envoyé à {}", to);
        } catch (Exception e) {
            log.warn("Impossible d'envoyer l'email à {} — token de réinitialisation (DEV) : {}", to, token);
        }
    }
}
