package com.cedric.jwtapi.controller;

import com.cedric.jwtapi.dto.AuthRequest;
import com.cedric.jwtapi.dto.AuthResponse;
import com.cedric.jwtapi.dto.ForgotPasswordRequest;
import com.cedric.jwtapi.dto.MessageResponse;
import com.cedric.jwtapi.dto.RefreshTokenRequest;
import com.cedric.jwtapi.dto.RegisterRequest;
import com.cedric.jwtapi.dto.ResetPasswordRequest;
import com.cedric.jwtapi.service.AuthService;
import com.cedric.jwtapi.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Inscription, connexion, renouvellement de token et gestion du mot de passe")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Inscription", description = "Crée un compte et retourne une paire de tokens JWT")
    @ApiResponse(responseCode = "201", description = "Compte créé")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "409", description = "Nom d'utilisateur ou email déjà utilisé")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion", description = "Authentifie un utilisateur et retourne une paire de tokens JWT")
    @ApiResponse(responseCode = "200", description = "Connexion réussie")
    @ApiResponse(responseCode = "401", description = "Identifiants incorrects")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renouveler les tokens", description = "Échange un refresh token valide contre une nouvelle paire de tokens")
    @ApiResponse(responseCode = "200", description = "Tokens renouvelés")
    @ApiResponse(responseCode = "401", description = "Refresh token invalide ou expiré")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request);
    }

    @PostMapping("/forgot-password")
    @Operation(
        summary = "Mot de passe oublié",
        description = """
            Génère un token de réinitialisation valable 15 minutes et l'envoie par email.
            La réponse est identique qu'un compte existe ou non (prévention d'énumération d'emails).
            """
    )
    @ApiResponse(responseCode = "200", description = "Demande traitée")
    @ApiResponse(responseCode = "400", description = "Email invalide")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestPasswordReset(request.email());
        return new MessageResponse(
            "Si un compte est associé à cet email, un lien de réinitialisation a été envoyé."
        );
    }

    @PostMapping("/reset-password")
    @Operation(
        summary = "Réinitialiser le mot de passe",
        description = "Applique un nouveau mot de passe à partir du token reçu par email. Le token est invalidé après usage."
    )
    @ApiResponse(responseCode = "200", description = "Mot de passe réinitialisé")
    @ApiResponse(responseCode = "400", description = "Données invalides ou token expiré")
    @ApiResponse(responseCode = "401", description = "Token invalide")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return new MessageResponse("Mot de passe réinitialisé avec succès.");
    }
}
