package com.example.plant_tracker.exception;

public class PlantAlreadyExistsException extends RuntimeException {

    public PlantAlreadyExistsException(String plantName) {
        super("Plant '" + plantName + "' already exists");
    }
}
