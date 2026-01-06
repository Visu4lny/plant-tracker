package com.example.plant_tracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Schema(example = "user@example.com", description = "User's email address")
        @NotBlank @Email
        String email,

        @Schema(example = "user123", description = "User's username")
        @Size(min = 3, max = 20)
        String username,

        @Schema(example = "securePassword123&*", description = "User's password")
        @NotBlank @Size(min = 8, max = 50)
        String password
) {
}
