package com.example.plant_tracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreatePlantRequest(
        @Schema(example = "Oleander", description = "Plant name")
        @NotBlank String name
) {
}
