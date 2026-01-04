package com.example.plant_tracker.exception;

public class PlantExistsException extends RuntimeException {

    public PlantExistsException(String plantName) {
        super("Plant '" + plantName + "' already exists");
    }
}
