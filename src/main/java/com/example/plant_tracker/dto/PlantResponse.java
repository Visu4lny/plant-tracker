package com.example.plant_tracker.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record PlantResponse(
        UUID id,
        String name,
        Instant lastWateredAt
) {
}
