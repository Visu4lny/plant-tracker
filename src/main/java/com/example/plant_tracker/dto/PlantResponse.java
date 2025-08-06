package com.example.plant_tracker.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlantResponse(
        UUID id,
        String name,
        LocalDateTime lastWatered
) {
}
