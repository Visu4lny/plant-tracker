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
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlantController.class)
class PlantControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PlantService plantService;

    @Test
    void createPlant_Returns201_WhenValidRequest() throws Exception {
        when(plantService.createPlant(any())).thenReturn(
                new PlantResponse(1L, "Oleander", null)
        );

        mockMvc.perform(post("/api/plants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Oleander\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
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
        PlantResponse plant1 = new PlantResponse(1L, "Paproć", null);
        PlantResponse plant2 = new PlantResponse(2L, "Mięta", null);

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
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(3);
        PlantResponse plant1 = new PlantResponse(1L, "Paproć", localDateTime);
        PlantResponse plant2 = new PlantResponse(2L, "Mięta", null);
        PlantResponse plant3 = new PlantResponse(3L, "Oleander", null);

        when(plantService.getAllPlants(Sort.Direction.ASC, "name")).thenReturn(List.of(plant2, plant3, plant1));

        mockMvc.perform(get("/api/plants"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$", hasSize(3)),
                        jsonPath("$[0].id").value(2),
                        jsonPath("$[0].name").value("Mięta"),
                        jsonPath("$[0].lastWatered").isEmpty(),
                        jsonPath("$[1].id").value(3),
                        jsonPath("$[1].name").value("Oleander"),
                        jsonPath("$[1].lastWatered").isEmpty(),
                        jsonPath("$[2].id").value(1),
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
        LocalDateTime localDateTime = LocalDateTime.now();

        when(plantService.updateLastWateredTime(1L, localDateTime)).thenReturn(
                new PlantResponse(1L, "Oleander", localDateTime)
        );

        mockMvc.perform(patch("/api/plants/1/last-watered")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"localDateTime\":\"" + localDateTime.toString() + "\"}"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(1),
                        jsonPath("$.lastWatered").value(localDateTime.toString())
                );
    }

    @Test
    void updateLastWateredTime_Returns404_WhenPlantNotFound() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.now();

        when(plantService.updateLastWateredTime(anyLong(), any()))
                .thenThrow(new PlantNotFoundException(2L));

        mockMvc.perform(patch("/api/plants/2/last-watered")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"localDateTime\":\"" + localDateTime.toString() + "\"}"))
                .andExpectAll(
                        status().isNotFound(),
                        content().string(containsString("Plant with id '2' does not exist"))
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
        doNothing().when(plantService).deletePlant(1L);

        mockMvc.perform(delete("/api/plants/1"))
                .andExpect(status().isNoContent());

        verify(plantService, times(1)).deletePlant(1L);
    }

    @Test
    void deletePlant_Returns404_WhenPlantMissing() throws Exception {
        doThrow(new PlantNotFoundException(2L))
                .when(plantService).deletePlant(2L);

        mockMvc.perform(delete("/api/plants/2"))
                .andExpectAll(
                        status().isNotFound(),
                        content().string(containsString("Plant with id '2' does not exist"))
                );
    }

    @Test
    void deletePlant_Returns400_WhenInvalidId() throws Exception {
        mockMvc.perform(delete("/api/plants/NaN"))
                .andExpect(status().isBadRequest());
    }
}