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
import org.springframework.data.domain.Sort;

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

    private List<UUID> plantIds;

    private void generateUUIDs(int amount) {
        plantIds = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            plantIds.add(UUID.randomUUID());
        }
    }
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
    void getAllPlants_ReturnsPlants_WhenPlantsExists() {
        generateUUIDs(3);
        LocalDateTime testTime = LocalDateTime.now();
        Plant plant1 = new Plant(plantIds.get(0), "Paproć", testTime.minusDays(1));
        Plant plant2 = new Plant(plantIds.get(1), "Oleander");
        Plant plant3 = new Plant(plantIds.get(2), "Mięta", testTime.minusDays(3));

        Sort.Direction direction = Sort.Direction.ASC;
        String property = "name";

        List<Plant> mockPlants = Arrays.asList(plant1, plant2, plant3);

        when(plantRepository.findAll(Sort.by(direction, property))).thenReturn(mockPlants);

        List<PlantResponse> result = plantService.getAllPlants(direction, property);

        assertThat(result)
                .hasSize(3)
                .extracting(PlantResponse::name)
                .containsExactly("Paproć", "Oleander", "Mięta");

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
        when(plantRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))).thenReturn(Collections.emptyList());
        assertThat(plantService.getAllPlants(Sort.Direction.ASC, "name")).isEmpty();
    }

    @Test
    void setLastWateredTime_SetsLastWateredTimeAndReturnsPlantResponse() {
        generateUUIDs(1);
        Plant plant = new Plant(plantIds.get(0), "Paproć");

        LocalDateTime lastWateredTime = LocalDateTime.now();
        when(plantRepository.findById(plantIds.get(0))).thenReturn(Optional.of(plant));

        PlantResponse result = plantService.updateLastWateredTime(plantIds.get(0), lastWateredTime);

        assertEquals(plantIds.get(0), result.id());
        assertEquals("Paproć", result.name());
        assertEquals(lastWateredTime, result.lastWatered());
    }

    @Test
    void setLastWateredTime_ThrowsPlantNotFoundException_WhenWateringNonExistentPlant() {
        generateUUIDs(1);
        LocalDateTime lastWateredTime = LocalDateTime.now();
        when(plantRepository.findById(plantIds.get(0))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> plantService.updateLastWateredTime(plantIds.get(0), lastWateredTime))
                .isInstanceOf(PlantNotFoundException.class)
                .hasMessageContaining(
                        "Plant with id '" + plantIds.get(0) + "' does not exist");
    }

    @Test
    void removePlant_RemovesPlant_WhenPlantExists() {
        generateUUIDs(1);

        Plant plant = new Plant(plantIds.get(0), "Paproć", LocalDateTime.now().minusDays(1));
        when(plantRepository.findById(plantIds.get(0))).thenReturn(Optional.of(plant));

        plantService.deletePlant(plantIds.get(0));

        verify(plantRepository).delete(plant);
    }

    @Test
    void removePlant_ThrowsPlantNotFoundException_WhenPlantNotFound() {
        generateUUIDs(1);
        when(plantRepository.findById(plantIds.get(0))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> plantService.deletePlant(plantIds.get(0)))
                .isInstanceOf(PlantNotFoundException.class)
                .hasMessageContaining(
                        "Plant with id '" + plantIds.get(0) + "' does not exist");

    }

}