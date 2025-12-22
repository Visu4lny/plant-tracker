package com.example.plant_tracker.controller;

import com.example.plant_tracker.dto.CreatePlantRequest;
import com.example.plant_tracker.dto.PlantResponse;
import com.example.plant_tracker.exception.PlantAlreadyExistsException;
import com.example.plant_tracker.exception.PlantNotFoundException;
import com.example.plant_tracker.security.SecurityConfig;
import com.example.plant_tracker.security.jwt.JwtUtils;
import com.example.plant_tracker.service.PlantService;
import com.example.plant_tracker.service.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlantController.class)
@Import(SecurityConfig.class)
class PlantControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PlantService plantService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    UserDetailsServiceImpl userDetailsService;

    private List<UUID> plantIds;

    private void generateUUIDs(int amount) {
        plantIds = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            plantIds.add(UUID.randomUUID());
        }
    }

    private final String email = "user@example.com";


    @Test
    @WithMockUser(username = "user@example.com")
    void createPlant_Returns201_WhenValidRequest() throws Exception {
        generateUUIDs(1);

        when(plantService.createPlant(any(), any())).thenReturn(
                new PlantResponse(plantIds.get(0), "Oleander", null)
        );

        mockMvc.perform(post("/api/plants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Oleander\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(plantIds.get(0).toString()));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void createPlant_Returns400_WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/plants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void createPlant_Returns409_WhenPlantAlreadyExists() throws Exception {
        when(plantService.createPlant(any(), eq(email)))
                .thenThrow(new PlantAlreadyExistsException("Oleander"));

        mockMvc.perform(post("/api/plants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Oleander\"}"))
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$").value("Plant 'Oleander' already exists")
                        );
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getAllPlants_Returns200WithPlants_WhenPlantsExist() throws Exception {
        generateUUIDs(2);
        PlantResponse plant1 = new PlantResponse(plantIds.get(0), "Paproć", null);
        PlantResponse plant2 = new PlantResponse(plantIds.get(1), "Mięta", null);

        when(plantService.getUserPlants(Sort.Direction.ASC, "name", email)).thenReturn(List.of(plant2, plant1));
        mockMvc.perform(get("/api/plants"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$").isArray(),
                        jsonPath("$").isNotEmpty(),
                        jsonPath("$", hasSize(2))
                );
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getAllPlants_ReturnsPlantsSortedByNameAsc_WhenNoSortParamGiven() throws Exception {
        generateUUIDs(3);
        Instant lastWateredAt = Instant.now().minus(Duration.ofDays(3));
        PlantResponse plant1 = new PlantResponse(plantIds.get(0), "Paproć", lastWateredAt);
        PlantResponse plant2 = new PlantResponse(plantIds.get(1), "Mięta", null);
        PlantResponse plant3 = new PlantResponse(plantIds.get(2), "Oleander", null);

        when(plantService.getUserPlants(Sort.Direction.ASC, "name", email))
                .thenReturn(List.of(plant2, plant3, plant1));

        mockMvc.perform(get("/api/plants"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$", hasSize(3)),
                        jsonPath("$[0].id").value(plantIds.get(1).toString()),
                        jsonPath("$[0].name").value("Mięta"),
                        jsonPath("$[0].lastWateredAt").isEmpty(),
                        jsonPath("$[1].id").value(plantIds.get(2).toString()),
                        jsonPath("$[1].name").value("Oleander"),
                        jsonPath("$[1].lastWateredAt").isEmpty(),
                        jsonPath("$[2].id").value(plantIds.get(0).toString()),
                        jsonPath("$[2].name").value("Paproć"),
                        jsonPath("$[2].lastWateredAt").value(lastWateredAt.toString())
                );
    }

    @Test
    void getAllPlants_Returns403_WhenInvalidSortDirection() throws Exception {
        mockMvc.perform(get("/api/plants?sort=name,upside_down"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getAllPlants_Returns200WithEmptyList_WhenNoPlantsExist() throws Exception {
        when(plantService.getUserPlants(any(), any(), eq(email)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/plants"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$").isArray(),
                        jsonPath("$").isEmpty()
                );
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void updateLastWateredTime_Returns200_WhenValid() throws Exception {
        generateUUIDs(1);
        Instant lastWateredAt = Instant.now();

        when(plantService.updateLastWateredTime(any(), eq(email))).thenReturn(
                new PlantResponse(plantIds.get(0), "Oleander", lastWateredAt)
        );

        mockMvc.perform(patch("/api/plants/" + plantIds.get(0) + "/last-watered")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"lastWateredAt\":\"" + lastWateredAt + "\"}"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(plantIds.get(0).toString()),
                        jsonPath("$.lastWateredAt").value(lastWateredAt.toString())
                );
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void updateLastWateredTime_Returns404_WhenPlantNotFound() throws Exception {
        generateUUIDs(1);
        Instant lastWateredAt = Instant.now();

        when(plantService.updateLastWateredTime(any(), eq(email)))
                .thenThrow(new PlantNotFoundException(plantIds.get(0)));

        mockMvc.perform(patch("/api/plants/" + plantIds.get(0) + "/last-watered")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"lastWateredAt\":\"" + lastWateredAt.toString() + "\"}"))
                .andExpectAll(
                        status().isNotFound(),
                        content().string(containsString(
                                "Plant with id '" + plantIds.get(0) + "' does not exist"))
                );
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void updateLastWateredTime_Returns400_WhenInvalidDate() throws Exception {

        mockMvc.perform(patch("/api/plants/1/last-watered")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"lastWateredAt\":\"invalid-date\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void deletePlant_Returns204_WhenPlantExists() throws Exception {
        generateUUIDs(1);

        doNothing().when(plantService).deletePlant(plantIds.get(0), email);

        mockMvc.perform(delete("/api/plants/" + plantIds.get(0)))
                .andExpect(status().isNoContent());

        verify(plantService, times(1)).deletePlant(plantIds.get(0), email);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void deletePlant_Returns404_WhenPlantMissing() throws Exception {
        generateUUIDs(1);

        doThrow(new PlantNotFoundException(plantIds.get(0)))
                .when(plantService).deletePlant(plantIds.get(0), email);

        mockMvc.perform(delete("/api/plants/" + plantIds.get(0))
                .with(csrf()))
                .andExpectAll(
                        status().isNotFound(),
                        content().string(containsString(
                                "Plant with id '" + plantIds.get(0) + "' does not exist"))
                );
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void deletePlant_Returns400_WhenInvalidId() throws Exception {
        mockMvc.perform(delete("/api/plants/NaN"))
                .andExpect(status().isBadRequest());
    }
}