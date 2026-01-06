package com.example.plant_tracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record PlantResponse(
        UUID id,
        @Schema(example = "Oleander", description = "Plant name")
        String name,
        Instant lastWateredAt
) {
}
