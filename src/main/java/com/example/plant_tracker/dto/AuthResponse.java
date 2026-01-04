package com.example.plant_tracker.dto;

import java.util.UUID;

public record AuthResponse(
        String jwt,
        String message,
        UUID userId
) {
    public AuthResponse(String message) {
        this(null, message, null);
    }
}
