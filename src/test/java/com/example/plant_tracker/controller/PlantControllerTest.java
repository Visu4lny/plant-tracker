package com.example.plant_tracker.controller;

import com.example.plant_tracker.dto.PlantResponse;
import com.example.plant_tracker.exception.PlantAlreadyExistsException;
import com.example.plant_tracker.exception.PlantNotFoundException;
import com.example.plant_tracker.service.PlantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlantController.class)
class PlantControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PlantService plantService;

    private List<UUID> plantIds;

    private void generateUUIDs(int amount) {
        plantIds = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            plantIds.add(UUID.randomUUID());
        }
    }

    @Test
    void createPlant_Returns201_WhenValidRequest() throws Exception {
        generateUUIDs(1);

        when(plantService.createPlant(any())).thenReturn(
                new PlantResponse(plantIds.get(0), "Oleander", null)
        );

        mockMvc.perform(post("/api/plants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Oleander\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(plantIds.get(0).toString()));
    }

    @Test
    void createPlant_Returns400_WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/plants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPlant_Returns409_WhenPlantAlreadyExists() throws Exception {
        when(plantService.createPlant(any()))
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
    void getAllPlants_Returns200WithPlants_WhenPlantsExist() throws Exception {
        generateUUIDs(2);
        PlantResponse plant1 = new PlantResponse(plantIds.get(0), "Paproć", null);
        PlantResponse plant2 = new PlantResponse(plantIds.get(1), "Mięta", null);

        when(plantService.getAllPlants(Sort.Direction.ASC, "name")).thenReturn(List.of(plant2, plant1));
        mockMvc.perform(get("/api/plants"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$").isArray(),
                        jsonPath("$").isNotEmpty(),
                        jsonPath("$", hasSize(2))
                );
    }

    @Test
    void getAllPlant_ReturnsPlantsSortedByNameAsc_WhenNoSortParamGiven() throws Exception {
        generateUUIDs(3);
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(3);
        PlantResponse plant1 = new PlantResponse(plantIds.get(0), "Paproć", localDateTime);
        PlantResponse plant2 = new PlantResponse(plantIds.get(1), "Mięta", null);
        PlantResponse plant3 = new PlantResponse(plantIds.get(2), "Oleander", null);

        when(plantService.getAllPlants(Sort.Direction.ASC, "name")).thenReturn(List.of(plant2, plant3, plant1));

        mockMvc.perform(get("/api/plants"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$", hasSize(3)),
                        jsonPath("$[0].id").value(plantIds.get(1).toString()),
                        jsonPath("$[0].name").value("Mięta"),
                        jsonPath("$[0].lastWatered").isEmpty(),
                        jsonPath("$[1].id").value(plantIds.get(2).toString()),
                        jsonPath("$[1].name").value("Oleander"),
                        jsonPath("$[1].lastWatered").isEmpty(),
                        jsonPath("$[2].id").value(plantIds.get(0).toString()),
                        jsonPath("$[2].name").value("Paproć"),
                        jsonPath("$[2].lastWatered").value(localDateTime.toString())
                );
    }

    @Test
    void getAllPlants_Returns200WithEmptyList_WhenNoPlantsExist() throws Exception {
        when(plantService.getAllPlants(Sort.Direction.ASC, "name")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/plants"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$").isArray(),
                        jsonPath("$").isEmpty()
                );
    }

    @Test
    void updateLastWateredTime_Returns200_WhenValid() throws Exception {
        generateUUIDs(1);
        LocalDateTime localDateTime = LocalDateTime.now();

        when(plantService.updateLastWateredTime(plantIds.get(0), localDateTime)).thenReturn(
                new PlantResponse(plantIds.get(0), "Oleander", localDateTime)
        );

        mockMvc.perform(patch("/api/plants/" + plantIds.get(0) + "/last-watered")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"localDateTime\":\"" + localDateTime.toString() + "\"}"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(plantIds.get(0).toString()),
                        jsonPath("$.lastWatered").value(localDateTime.toString())
                );
    }

    @Test
    void updateLastWateredTime_Returns404_WhenPlantNotFound() throws Exception {
        generateUUIDs(1);
        LocalDateTime localDateTime = LocalDateTime.now();

        when(plantService.updateLastWateredTime(any(), any()))
                .thenThrow(new PlantNotFoundException(plantIds.get(0)));

        mockMvc.perform(patch("/api/plants/" + plantIds.get(0) + "/last-watered")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"localDateTime\":\"" + localDateTime.toString() + "\"}"))
                .andExpectAll(
                        status().isNotFound(),
                        content().string(containsString(
                                "Plant with id '" + plantIds.get(0) + "' does not exist"))
                );
    }

    @Test
    void updateLastWateredTime_Returns400_WhenInvalidDate() throws Exception {

        mockMvc.perform(patch("/api/plants/1/last-watered")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"localDateTime\":\"invalid-date\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletePlant_Returns204_WhenPlantExists() throws Exception {
        generateUUIDs(1);
        doNothing().when(plantService).deletePlant(plantIds.get(0));

        mockMvc.perform(delete("/api/plants/" + plantIds.get(0)))
                .andExpect(status().isNoContent());

        verify(plantService, times(1)).deletePlant(plantIds.get(0));
    }

    @Test
    void deletePlant_Returns404_WhenPlantMissing() throws Exception {
        generateUUIDs(1);
        doThrow(new PlantNotFoundException(plantIds.get(0)))
                .when(plantService).deletePlant(plantIds.get(0));

        mockMvc.perform(delete("/api/plants/" + plantIds.get(0)))
                .andExpectAll(
                        status().isNotFound(),
                        content().string(containsString(
                                "Plant with id '" + plantIds.get(0) + "' does not exist"))
                );
    }

    @Test
    void deletePlant_Returns400_WhenInvalidId() throws Exception {
        mockMvc.perform(delete("/api/plants/NaN"))
                .andExpect(status().isBadRequest());
    }
}