package com.example.plant_tracker.service;

import com.example.plant_tracker.dto.AuthResponse;
import com.example.plant_tracker.dto.LoginRequest;
import com.example.plant_tracker.dto.RegisterRequest;
import com.example.plant_tracker.exception.EmailExistsException;
import com.example.plant_tracker.model.User;
import com.example.plant_tracker.security.jwt.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils,
                       UserService userService,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest request) throws EmailExistsException {
        if (userService.existsByEmail(request.email())) {
            throw new EmailExistsException(request.email());
        }

        User user = createUser(request);

        User savedUser = userService.createUser(user);
        String jwt = jwtUtils.generateToken(savedUser.getEmail());

        return new AuthResponse(jwt, "User registered successfully", savedUser.getId());
    }

    public AuthResponse login(@Valid LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        String jwt = jwtUtils.generateToken(request.email());
        return new AuthResponse(jwt, "Login successful", null);
    }

    private User createUser(@Valid RegisterRequest request) {
        User user = new User();
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("ROLE_USER");
        return user;
    }
}
