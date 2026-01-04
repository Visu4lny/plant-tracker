package com.example.plant_tracker.service;

import com.example.plant_tracker.dto.AuthResponse;
import com.example.plant_tracker.dto.LoginRequest;
import com.example.plant_tracker.dto.RegisterRequest;
import com.example.plant_tracker.exception.EmailExistsException;
import com.example.plant_tracker.model.User;
import com.example.plant_tracker.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(authenticationManager, jwtUtils, userService, passwordEncoder);
    }

    @Test
    void register_Success_ReturnsAuthResponse() throws EmailExistsException {
        RegisterRequest request = new RegisterRequest("test@example.com", "user", "password123");
        User savedUser = createTestUser("test@example.com", "user");

        when(userService.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userService.createUser(any(User.class))).thenReturn(savedUser);
        when(jwtUtils.generateToken("test@example.com")).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.jwt());
        assertEquals("User registered successfully", response.message());
    }

    @Test
    void register_EmailExists_ThrowsException() {
        RegisterRequest request = new RegisterRequest("test@example.com", "user", "password123");

        when(userService.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(EmailExistsException.class, () -> authService.register(request));

    }

    @Test
    void login_Success_ReturnsAuthResponse() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(jwtUtils.generateToken("test@example.com")).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.jwt());
        assertEquals("Login successful", response.message());
    }

    @Test
    void login_InvalidCredentials_ThrowsException() {
        LoginRequest request = new LoginRequest("user@test.com", "wrongpass");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    private User createTestUser(String email, String username) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword("encoded_password");
        user.setRole("ROLE_USER");
        return user;
    }
}
