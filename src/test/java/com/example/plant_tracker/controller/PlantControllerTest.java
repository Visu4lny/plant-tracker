package com.example.plant_tracker.controller;

import com.example.plant_tracker.dto.PlantResponse;
import com.example.plant_tracker.exception.PlantExistsException;
import com.example.plant_tracker.exception.PlantNotFoundException;
import com.example.plant_tracker.security.SecurityConfig;
import com.example.plant_tracker.security.jwt.JwtUtils;
import com.example.plant_tracker.service.PlantService;
import com.example.plant_tracker.service.UserDetailsServiceImpl;
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

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    UserDetailsServiceImpl userDetailsService;

    private final String email = "user@example.com";

    @Test
    @WithMockUser(username = "user@example.com")
    void createPlant_Returns201_WhenValidRequest() throws Exception {
        UUID plantId = UUID.randomUUID();

        when(plantService.createPlant(any(), any())).thenReturn(
                new PlantResponse(plantId, "Oleander", null)
        );

        mockMvc.perform(post("/api/plants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Oleander\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(plantId.toString()));
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
                .thenThrow(new PlantExistsException("Oleander"));

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
        UUID plantId1 = UUID.randomUUID();
        UUID plantId2 = UUID.randomUUID();
        PlantResponse plant1 = new PlantResponse(plantId1, "Paproć", null);
        PlantResponse plant2 = new PlantResponse(plantId2, "Mięta", null);

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
        UUID plantId1 = UUID.randomUUID();
        UUID plantId2 = UUID.randomUUID();
        UUID plantId3 = UUID.randomUUID();
        Instant lastWateredAt = Instant.now().minus(Duration.ofDays(3));
        PlantResponse plant1 = new PlantResponse(plantId1, "Paproć", lastWateredAt);
        PlantResponse plant2 = new PlantResponse(plantId2, "Mięta", null);
        PlantResponse plant3 = new PlantResponse(plantId3, "Oleander", null);

        when(plantService.getUserPlants(Sort.Direction.ASC, "name", email))
                .thenReturn(List.of(plant2, plant3, plant1));

        mockMvc.perform(get("/api/plants"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$", hasSize(3)),
                        jsonPath("$[0].id").value(plantId2.toString()),
                        jsonPath("$[0].name").value("Mięta"),
                        jsonPath("$[0].lastWateredAt").isEmpty(),
                        jsonPath("$[1].id").value(plantId3.toString()),
                        jsonPath("$[1].name").value("Oleander"),
                        jsonPath("$[1].lastWateredAt").isEmpty(),
                        jsonPath("$[2].id").value(plantId1.toString()),
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
    void updateLastWateredAt_Returns200_WhenValid() throws Exception {
        UUID plantId = UUID.randomUUID();
        Instant lastWateredAt = Instant.now();

        when(plantService.updateLastWateredAt(any(), eq(email))).thenReturn(
                new PlantResponse(plantId, "Oleander", lastWateredAt)
        );

        mockMvc.perform(patch("/api/plants/" + plantId + "/last-watered")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"lastWateredAt\":\"" + lastWateredAt + "\"}"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(plantId.toString()),
                        jsonPath("$.lastWateredAt").value(lastWateredAt.toString())
                );
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void updateLastWateredAt_Returns404_WhenPlantNotFound() throws Exception {
        UUID plantId = UUID.randomUUID();
        Instant lastWateredAt = Instant.now();

        when(plantService.updateLastWateredAt(any(), eq(email)))
                .thenThrow(new PlantNotFoundException(plantId));

        mockMvc.perform(patch("/api/plants/" + plantId + "/last-watered")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"lastWateredAt\":\"" + lastWateredAt.toString() + "\"}"))
                .andExpectAll(
                        status().isNotFound(),
                        content().string(containsString(
                                "Plant with id '" + plantId + "' does not exist"))
                );
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void updateLastWateredAt_Returns400_WhenInvalidDate() throws Exception {

        mockMvc.perform(patch("/api/plants/1/last-watered")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"lastWateredAt\":\"invalid-date\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void deletePlant_Returns204_WhenPlantExists() throws Exception {
        UUID plantId = UUID.randomUUID();

        doNothing().when(plantService).deletePlant(plantId, email);

        mockMvc.perform(delete("/api/plants/" + plantId))
                .andExpect(status().isNoContent());

        verify(plantService, times(1)).deletePlant(plantId, email);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void deletePlant_Returns404_WhenPlantMissing() throws Exception {
        UUID plantId = UUID.randomUUID();

        doThrow(new PlantNotFoundException(plantId))
                .when(plantService).deletePlant(plantId, email);

        mockMvc.perform(delete("/api/plants/" + plantId)
                .with(csrf()))
                .andExpectAll(
                        status().isNotFound(),
                        content().string(containsString(
                                "Plant with id '" + plantId + "' does not exist"))
                );
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void deletePlant_Returns400_WhenInvalidId() throws Exception {
        mockMvc.perform(delete("/api/plants/NaN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void updatePlantName_Returns200_WhenValid() throws Exception {
        UUID plantId = UUID.randomUUID();
        String newName = "NewPlantName";

        when(plantService.updatePlantName(any(), eq(email), eq(newName))).thenReturn(
                new PlantResponse(plantId, newName, null)
        );

        mockMvc.perform(patch("/api/plants/" + plantId + "/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + newName + "\"}"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(plantId.toString()),
                        jsonPath("$.name").value(newName)
                );
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void updatePlantName_Returns400_WhenNameIsBlank() throws Exception {
        UUID plantId = UUID.randomUUID();

        mockMvc.perform(patch("/api/plants/" + plantId + "/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}