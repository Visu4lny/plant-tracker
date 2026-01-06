package com.example.plant_tracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record AuthResponse(
        @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30"
                , description = "JWT token")
        String jwt,
        @Schema(example = "User registered successfully", description = "Auth response message")
        String message,
        UUID userId
) {
    public AuthResponse(String message) {
        this(null, message, null);
    }
}
