package com.example.plant_tracker.service;

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
    void should_AddNewPlant() {
        Plant newPlant = new Plant("Paproć");
        when(plantRepository.save(any(Plant.class))).thenReturn(newPlant);

        PlantResponse createdPlant = plantService.createPlant("Paproć");

        assertEquals(createdPlant.name(), newPlant.getName());
        verify(plantRepository).save(any(Plant.class));
    }

    @Test
    void should_ThrowPlantAlreadyExistsException_When_AddingPlantWithAlreadyExistingName() {
        String name = "Paproć";
        when(plantRepository.existsByName(name)).thenReturn(true);

        assertThatThrownBy(() -> plantService.createPlant(name))
                .isInstanceOf(PlantAlreadyExistsException.class)
                .hasMessageContaining("Plant '" + name + "' already exists");
    }

    @Test
    void should_ReturnAllPlants_When_RepositoryHasPlants() {
        LocalDateTime testTime = LocalDateTime.now();
        Plant fern = new Plant(1L, "Paproć", testTime.minusDays(1));
        Plant oleander = new Plant(2L, "Oleander");
        Plant oleanderTree = new Plant(3L, "Oleander - pień", testTime.minusDays(3));

        List<Plant> mockPlants = Arrays.asList(fern, oleander, oleanderTree);

        when(plantRepository.findAll()).thenReturn(mockPlants);

        List<Plant> result = plantService.getAllPlants();

        assertThat(result)
                .hasSize(3)
                .extracting(Plant::getName)
                .containsExactly("Paproć", "Oleander", "Oleander - pień");

        assertThat(result.get(0))
                .extracting(Plant::getLastWateredTime)
                .isEqualTo(testTime.minusDays(1));

        assertThat(result.get(2))
                .extracting(Plant::getLastWateredTime)
                .isEqualTo(testTime.minusDays(3));

        assertThat(result.get(1))
                .extracting(Plant::getLastWateredTime)
                .isNull();
    }

    @Test
    void should_ReturnEmptyList_When_NoPlantsExist() {
        when(plantRepository.findAll()).thenReturn(Collections.emptyList());
        assertThat(plantService.getAllPlants()).isEmpty();
    }

    @Test
    void should_SetLastWateredTime() {
        Plant plant = new Plant(1L, "Paproć");

        LocalDateTime lastWateredTime = LocalDateTime.now();
        when(plantRepository.findById(1L)).thenReturn(Optional.of(plant));

        Plant result = plantService.setLastWateredTime(1L, lastWateredTime);

        assertEquals(1L, result.getId());
        assertEquals("Paproć", result.getName());
        assertEquals(lastWateredTime, result.getLastWateredTime());
    }

    @Test
    void should_ThrowPlantNotFoundException_When_WateringNonExsistentPlant() {
        LocalDateTime lastWateredTime = LocalDateTime.now();
        when(plantRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> plantService.setLastWateredTime(1L, lastWateredTime))
                .isInstanceOf(PlantNotFoundException.class)
                .hasMessageContaining("Plant with id '" + 1L + "' does not exist");
    }

    @Test
    void should_RemovePlant_When_PlantExists() {
        Long plantId = 1L;

        Plant plant = new Plant(1L, "Paproć", LocalDateTime.now().minusDays(1));
        when(plantRepository.findById(1L)).thenReturn(Optional.of(plant));

        plantService.removePlant(plantId);

        verify(plantRepository).delete(plant);
    }

    @Test
    void should_ThrowPlantNotFoundException_When_PlantNotFound() {
        Long nonExistentPlantId = 1L;
        when(plantRepository.findById(nonExistentPlantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> plantService.removePlant(nonExistentPlantId))
                .isInstanceOf(PlantNotFoundException.class)
                .hasMessageContaining("Plant with id '" + nonExistentPlantId + "' does not exist");

    }

}