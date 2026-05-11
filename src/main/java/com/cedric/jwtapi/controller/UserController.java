package com.cedric.jwtapi.controller;

import com.cedric.jwtapi.dto.ChangePasswordRequest;
import com.cedric.jwtapi.dto.MessageResponse;
import com.cedric.jwtapi.dto.UserResponse;
import com.cedric.jwtapi.entity.User;
import com.cedric.jwtapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs", description = "Gestion des profils utilisateurs")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Mon profil", description = "Retourne le profil de l'utilisateur authentifié")
    @ApiResponse(responseCode = "200", description = "Profil retourné")
    @ApiResponse(responseCode = "401", description = "Non authentifié")
    public UserResponse getCurrentUser(@AuthenticationPrincipal User user) {
        return UserResponse.from(user);
    }

    @PutMapping("/change-password")
    @Operation(
        summary = "Changer le mot de passe",
        description = "Modifie le mot de passe de l'utilisateur authentifié. Le mot de passe actuel doit être fourni pour confirmer l'identité."
    )
    @ApiResponse(responseCode = "200", description = "Mot de passe modifié")
    @ApiResponse(responseCode = "400", description = "Données invalides ou nouveau mot de passe identique à l'actuel")
    @ApiResponse(responseCode = "401", description = "Mot de passe actuel incorrect ou utilisateur non authentifié")
    public MessageResponse changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(user, request.currentPassword(), request.newPassword());
        return new MessageResponse("Mot de passe modifié avec succès.");
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Liste des utilisateurs", description = "Retourne tous les utilisateurs (ROLE_ADMIN uniquement)")
    @ApiResponse(responseCode = "200", description = "Liste retournée")
    @ApiResponse(responseCode = "403", description = "Accès refusé")
    public List<UserResponse> getAllUsers() {
        return userService.findAll();
    }
}
