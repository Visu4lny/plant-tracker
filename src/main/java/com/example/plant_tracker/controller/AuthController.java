package com.example.plant_tracker.controller;

import com.example.plant_tracker.dto.AuthResponse;
import com.example.plant_tracker.dto.LoginRequest;
import com.example.plant_tracker.dto.RegisterRequest;
import com.example.plant_tracker.exception.EmailExistsException;
import com.example.plant_tracker.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) throws EmailExistsException {
        log.info("Registration attempt for email: {}", request.email());

        AuthResponse response = authService.register(request);
        log.info("User registered: {}", request.email());

        URI location = buildUserUri(response.userId());

        return ResponseEntity.created(location)
                .body(response);
    }

    private static URI buildUserUri(UUID userId) {
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(userId)
                .toUri();
        return location;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        AuthResponse response = authService.login(request);
        log.info("Login successful for: {}", request.email());

        return ResponseEntity.ok(response);
    }
}
