package com.example.plant_tracker.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank String email,
        @NotBlank String username,
        @NotBlank String password
) {
}
