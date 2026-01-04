package com.example.plant_tracker.controller;

import com.example.plant_tracker.dto.AuthResponse;
import com.example.plant_tracker.dto.LoginRequest;
import com.example.plant_tracker.dto.RegisterRequest;
import com.example.plant_tracker.exception.EmailExistsException;
import com.example.plant_tracker.exception.GlobalExceptionHandler;
import com.example.plant_tracker.model.User;
import com.example.plant_tracker.security.SecurityConfig;
import com.example.plant_tracker.security.jwt.JwtAuthFilter;
import com.example.plant_tracker.security.jwt.JwtUtils;
import com.example.plant_tracker.service.AuthService;
import com.example.plant_tracker.service.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void register_Success_ReturnsJwtAndCreatedStatus() throws Exception, EmailExistsException {
        RegisterRequest request = new RegisterRequest("test@example.com", "user", "password123");
        User savedUser = createTestUser("test@example.com", "user");
        AuthResponse response = new AuthResponse("jwt-token", "User registered successfully", savedUser.getId());

        when(authService.register(request)).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.jwt").value("jwt-token"),
                        jsonPath("$.message").value("User registered successfully"),
                        jsonPath("$.userId").exists(),
                        header().exists("Location")
                );
    }

    @Test
    void register_EmailExists_ReturnsConflict() throws Exception, EmailExistsException {
        RegisterRequest request = new RegisterRequest("test@example.com", "user", "password123");

        when(authService.register(request))
                .thenThrow(new EmailExistsException("test@example.com"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.jwt").isEmpty(),
                        jsonPath("$.message").value("Email already exists")
                );
    }

    @Test
    void register_BlankEmail_ReturnsBadRequest() throws Exception {
        String invalidRequest = "{\"email\":\"\",\"username\":\"user\",\"password\":\"password123\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_InvalidEmailFormat_ReturnsBadRequest() throws Exception {
        String invalidRequest = "{\"email\":\"not-an-email\",\"username\":\"user\",\"password\":\"password123\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ShortUsername_ReturnsBadRequest() throws Exception {
        String invalidRequest = "{\"email\":\"test@example.com\",\"username\":\"ab\",\"password\":\"password123\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ShortPassword_ReturnsBadRequest() throws Exception {
        String invalidRequest = "{\"email\":\"test@example.com\",\"username\":\"user\",\"password\":\"short\"}";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        AuthResponse response = new AuthResponse("jwt-token", "Login successful", null);

        when(authService.login(request)).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.jwt").value("jwt-token"),
                        jsonPath("$.message").value("Login successful")
                );
    }

    @Test
    void login_InvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        when(authService.login(request))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.jwt").isEmpty(),
                        jsonPath("$.message").value("Invalid credentials")
                );
    }

    @Test
    void login_BlankEmail_ReturnsBadRequest() throws Exception {
        String invalidRequest = "{\"email\":\"\",\"password\":\"password123\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ShortPassword_ReturnsBadRequest() throws Exception {
        String invalidRequest = "{\"email\":\"test@example.com\",\"password\":\"short\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
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
