package com.cedric.jwtapi.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(

        @NotBlank(message = "Le nom d'utilisateur est requis")
        String username,

        @NotBlank(message = "Le mot de passe est requis")
        String password
) {}
