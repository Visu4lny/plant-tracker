package com.example.plant_tracker.exception;

import java.util.UUID;

public class PlantNotFoundException extends RuntimeException {

    public PlantNotFoundException(UUID id) {
        super("Plant with id '" + id + "' does not exist");
    }
}
