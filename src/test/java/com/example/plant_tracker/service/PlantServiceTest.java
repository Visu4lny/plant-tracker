package com.example.plant_tracker.service;

import com.example.plant_tracker.model.Plant;
import com.example.plant_tracker.repository.PlantRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    void should_CreateNewPlant() {
        Plant newPlant = new Plant("Paproć");
        when(plantRepository.save(any(Plant.class))).thenReturn(newPlant);

        Plant created = plantService.addPlant("Paproć");

        assertEquals(created.getName(), newPlant.getName());
        verify(plantRepository).save(any(Plant.class));

    }

}