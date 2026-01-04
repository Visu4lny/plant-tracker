package com.example.plant_tracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email String email,
        @Size(min = 3, max = 20) String username,
        @NotBlank @Size(min = 8, max = 50) String password
) {
}
