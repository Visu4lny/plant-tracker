package com.example.plant_tracker.dto;

import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

public record PlantResponse(
        Long id,
        String name,
        LocalDateTime lastWatered
) {
}
