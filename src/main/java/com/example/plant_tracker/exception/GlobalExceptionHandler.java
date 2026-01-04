package com.example.plant_tracker.exception;

import com.example.plant_tracker.dto.AuthResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@ControllerAdvice
@RestController
public class GlobalExceptionHandler {

    @ExceptionHandler(PlantExistsException.class)
    public ResponseEntity<String> handlePlantExists(
            PlantExistsException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ex.getMessage());
    }

    @ExceptionHandler(PlantNotFoundException.class)
    public ResponseEntity<String> handlePlantNotFound(
            PlantNotFoundException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(EmailExistsException.class)
    public ResponseEntity<AuthResponse> handleEmailExists(
            EmailExistsException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new AuthResponse("", "Email already exists", null));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<AuthResponse> handleBadCredentials(
            BadCredentialsException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new AuthResponse("", "Invalid credentials", null));
    }
}
