package com.example.plant_tracker.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record UpdateLastWateredRequest(
        @NotNull
        Instant lastWateredAt
) {
}
