package com.example.plant_tracker.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record UpdateLastWateredRequest(
        @NotNull
        LocalDateTime localDateTime
) {
}
