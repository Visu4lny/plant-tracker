package com.example.plant_tracker.exception;

public class PlantNotFoundException extends RuntimeException {

    public PlantNotFoundException(Long id) {
        super("Plant with id '" + id + "' does not exist");
    }
}
