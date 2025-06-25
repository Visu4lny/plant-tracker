package com.example.plant_tracker.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePlantRequest(
        @NotBlank String name
) {
}
