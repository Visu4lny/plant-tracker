package com.example.plant_tracker.service;

import com.example.plant_tracker.dto.CreatePlantRequest;
import com.example.plant_tracker.dto.PlantResponse;
import com.example.plant_tracker.exception.PlantAlreadyExistsException;
import com.example.plant_tracker.exception.PlantNotFoundException;
import com.example.plant_tracker.model.Plant;
import com.example.plant_tracker.repository.PlantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlantServiceTest {

    @Mock
    private PlantRepository plantRepository;

    private PlantService plantService;

    @BeforeEach
    void setUp() {
        plantService = new PlantService(plantRepository);
    }

    @Test
    void createPlant_CreatesPlantAndReturnsPlantResponse() {
        Plant newPlant = new Plant("Paproć");
        when(plantRepository.save(any(Plant.class))).thenReturn(newPlant);

        CreatePlantRequest request = new CreatePlantRequest("Paproć");

        PlantResponse createdPlant = plantService.createPlant(request);

        assertEquals(createdPlant.name(), newPlant.getName());
        verify(plantRepository).save(any(Plant.class));
    }

    @Test
    void createPlant_ThrowsPlantAlreadyExistsException_WhenPlantAlreadyExists() {
        CreatePlantRequest request = new CreatePlantRequest("Paproć");
        when(plantRepository.existsByName(request.name())).thenReturn(true);

        assertThatThrownBy(() -> plantService.createPlant(request))
                .isInstanceOf(PlantAlreadyExistsException.class)
                .hasMessageContaining("Plant '" + request.name() + "' already exists");
    }

    @Test
    void getAllPlants_ReturnsPlants_WhenPlantsExsist() {
        LocalDateTime testTime = LocalDateTime.now();
        Plant fern = new Plant(1L, "Paproć", testTime.minusDays(1));
        Plant oleander = new Plant(2L, "Oleander");
        Plant oleanderTree = new Plant(3L, "Oleander - pień", testTime.minusDays(3));

        List<Plant> mockPlants = Arrays.asList(fern, oleander, oleanderTree);

        when(plantRepository.findAll()).thenReturn(mockPlants);

        List<PlantResponse> result = plantService.getAllPlants();

        assertThat(result)
                .hasSize(3)
                .extracting(PlantResponse::name)
                .containsExactly("Paproć", "Oleander", "Oleander - pień");

        assertThat(result.get(0))
                .extracting(PlantResponse::lastWatered)
                .isEqualTo(testTime.minusDays(1));

        assertThat(result.get(2))
                .extracting(PlantResponse::lastWatered)
                .isEqualTo(testTime.minusDays(3));

        assertThat(result.get(1))
                .extracting(PlantResponse::lastWatered)
                .isNull();
    }

    @Test
    void getAllPlants_ReturnsEmptyList_WhenNoPlantsExist() {
        when(plantRepository.findAll()).thenReturn(Collections.emptyList());
        assertThat(plantService.getAllPlants()).isEmpty();
    }

    @Test
    void setLastWateredTime_SetsLastWateredTimeAndReturnsPlantResponse() {
        Plant plant = new Plant(1L, "Paproć");

        LocalDateTime lastWateredTime = LocalDateTime.now();
        when(plantRepository.findById(1L)).thenReturn(Optional.of(plant));

        PlantResponse result = plantService.updateLastWateredTime(1L, lastWateredTime);

        assertEquals(1L, result.id());
        assertEquals("Paproć", result.name());
        assertEquals(lastWateredTime, result.lastWatered());
    }

    @Test
    void setLastWateredTime_ThrowsPlantNotFoundException_WhenWateringNonExsistentPlant() {
        LocalDateTime lastWateredTime = LocalDateTime.now();
        when(plantRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> plantService.updateLastWateredTime(1L, lastWateredTime))
                .isInstanceOf(PlantNotFoundException.class)
                .hasMessageContaining("Plant with id '" + 1L + "' does not exist");
    }

    @Test
    void removePlant_RemovesPlant_WhenPlantExists() {
        Long plantId = 1L;

        Plant plant = new Plant(1L, "Paproć", LocalDateTime.now().minusDays(1));
        when(plantRepository.findById(1L)).thenReturn(Optional.of(plant));

        plantService.deletePlant(plantId);

        verify(plantRepository).delete(plant);
    }

    @Test
    void removePlant_ThrowsPlantNotFoundException_WhenPlantNotFound() {
        Long nonExistentPlantId = 1L;
        when(plantRepository.findById(nonExistentPlantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> plantService.deletePlant(nonExistentPlantId))
                .isInstanceOf(PlantNotFoundException.class)
                .hasMessageContaining("Plant with id '" + nonExistentPlantId + "' does not exist");

    }

}