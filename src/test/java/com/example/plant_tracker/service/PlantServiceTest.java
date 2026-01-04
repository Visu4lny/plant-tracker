package com.example.plant_tracker.service;

import com.example.plant_tracker.dto.CreatePlantRequest;
import com.example.plant_tracker.dto.PlantResponse;
import com.example.plant_tracker.exception.PlantExistsException;
import com.example.plant_tracker.exception.PlantNotFoundException;
import com.example.plant_tracker.model.Plant;
import com.example.plant_tracker.model.User;
import com.example.plant_tracker.repository.PlantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
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
    @Mock
    private UserService userService;

    private PlantService plantService;

    private List<UUID> plantIds;

    private void generateUUIDs(int amount) {
        plantIds = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            plantIds.add(UUID.randomUUID());
        }
    }

    private final String email = "test@test.com";
    private final User user = new User(UUID.randomUUID(), email, "username", "password", "user", new ArrayList<>());
    @BeforeEach
    void setUp() {
        plantService = new PlantService(plantRepository, userService);
    }

    @Test
    void createPlant_CreatesPlantAndReturnsPlantResponse() {
        Plant newPlant = new Plant("Paproć");
        when(plantRepository.save(any(Plant.class))).thenReturn(newPlant);
        when(userService.findByEmail(email)).thenReturn(user);

        CreatePlantRequest request = new CreatePlantRequest("Paproć");

        PlantResponse createdPlant = plantService.createPlant(request, email);

        assertEquals(createdPlant.name(), newPlant.getName());
        verify(plantRepository).save(any(Plant.class));
    }

    @Test
    void createPlant_ThrowsPlantAlreadyExistsException_WhenPlantAlreadyExists() {
        CreatePlantRequest request = new CreatePlantRequest("Paproć");
        when(plantRepository.existsByName(request.name())).thenReturn(true);

        assertThatThrownBy(() -> plantService.createPlant(request, email))
                .isInstanceOf(PlantExistsException.class)
                .hasMessageContaining("Plant '" + request.name() + "' already exists");
    }

    @Test
    void getAllPlants_ReturnsPlants_WhenPlantsExists() {
        generateUUIDs(3);
        Instant lastWateredAt = Instant.now();
        Plant plant1 = new Plant(plantIds.get(0), "Paproć", lastWateredAt.minus(Duration.ofDays(1)));
        Plant plant2 = new Plant(plantIds.get(1), "Oleander");
        Plant plant3 = new Plant(plantIds.get(2), "Mięta", lastWateredAt.minus(Duration.ofDays(3)));

        Sort.Direction direction = Sort.Direction.ASC;
        String property = "name";

        List<Plant> mockPlants = Arrays.asList(plant1, plant2, plant3);

        when(plantRepository.findAllByUser(user, Sort.by(direction, property))).thenReturn(mockPlants);
        when(userService.findByEmail(email)).thenReturn(user);

        List<PlantResponse> result = plantService.getUserPlants(direction, property, email);

        assertThat(result)
                .hasSize(3)
                .extracting(PlantResponse::name)
                .containsExactly("Paproć", "Oleander", "Mięta");

        assertThat(result.get(0))
                .extracting(PlantResponse::lastWateredAt)
                .isEqualTo(lastWateredAt.minus(Duration.ofDays(1)));

        assertThat(result.get(2))
                .extracting(PlantResponse::lastWateredAt)
                .isEqualTo(lastWateredAt.minus(Duration.ofDays(3)));

        assertThat(result.get(1))
                .extracting(PlantResponse::lastWateredAt)
                .isNull();
    }

    @Test
    void getAllPlants_ReturnsEmptyList_WhenNoPlantsExist() {
        when(plantRepository.findAllByUser(user, Sort.by(Sort.Direction.ASC, "name"))).thenReturn(Collections.emptyList());
        when(userService.findByEmail(email)).thenReturn(user);

        assertThat(plantService.getUserPlants(Sort.Direction.ASC, "name", email)).isEmpty();
    }

    @Test
    void setLastWateredTime_SetsLastWateredTimeAndReturnsPlantResponse() {
        generateUUIDs(1);
        Plant plant = new Plant(plantIds.get(0), "Paproć");
        Instant lastWateredAt = Instant.now();
        when(plantRepository.findByIdAndUser(plantIds.get(0), user)).thenReturn(Optional.of(plant));
        when(userService.findByEmail(email)).thenReturn(user);

        PlantResponse result = plantService.updateLastWateredTime(plantIds.get(0), email);

        assertEquals(plantIds.get(0), result.id());
        assertEquals("Paproć", result.name());
        assertEquals(lastWateredAt.atZone(ZoneOffset.UTC).toLocalDate(),
                result.lastWateredAt().atZone(ZoneOffset.UTC).toLocalDate());
    }

    @Test
    void setLastWateredTime_ThrowsPlantNotFoundException_WhenWateringNonExistentPlant() {
        generateUUIDs(1);
        when(plantRepository.findByIdAndUser(plantIds.get(0), user)).thenReturn(Optional.empty());
        when(userService.findByEmail(email)).thenReturn(user);

        assertThatThrownBy(() -> plantService.updateLastWateredTime(plantIds.get(0), email))
                .isInstanceOf(PlantNotFoundException.class)
                .hasMessageContaining(
                        "Plant with id '" + plantIds.get(0) + "' does not exist");
    }

    @Test
    void removePlant_RemovesPlant_WhenPlantExists() {
        generateUUIDs(1);

        Plant plant = new Plant(plantIds.get(0), "Paproć", Instant.now().minus(Duration.ofDays(1)));
        when(plantRepository.findByIdAndUser(plantIds.get(0), user)).thenReturn(Optional.of(plant));
        when(userService.findByEmail(email)).thenReturn(user);

        plantService.deletePlant(plantIds.get(0), email);

        verify(plantRepository).delete(plant);
    }

    @Test
    void removePlant_ThrowsPlantNotFoundException_WhenPlantNotFound() {
        generateUUIDs(1);
        when(plantRepository.findByIdAndUser(plantIds.get(0), user)).thenReturn(Optional.empty());
        when(userService.findByEmail(email)).thenReturn(user);

        assertThatThrownBy(() -> plantService.deletePlant(plantIds.get(0), email))
                .isInstanceOf(PlantNotFoundException.class)
                .hasMessageContaining(
                        "Plant with id '" + plantIds.get(0) + "' does not exist");

    }

}