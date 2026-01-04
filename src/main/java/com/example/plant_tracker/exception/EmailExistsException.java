package com.example.plant_tracker.exception;

public class EmailExistsException extends RuntimeException {
    public EmailExistsException(String email) {
        super("Email already exists: " + email);
    }
}
