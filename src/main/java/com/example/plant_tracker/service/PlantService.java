package com.example.plant_tracker.service;

import com.example.plant_tracker.dto.PlantResponse;
import com.example.plant_tracker.exception.PlantAlreadyExistsException;
import com.example.plant_tracker.exception.PlantNotFoundException;
import com.example.plant_tracker.model.Plant;
import com.example.plant_tracker.repository.PlantRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PlantService {

    private final PlantRepository plantRepository;

    public PlantService(PlantRepository plantRepository) {
        this.plantRepository = plantRepository;
    }


    public PlantResponse createPlant(String name) {
        if (plantRepository.existsByName(name)) {
            throw new PlantAlreadyExistsException(name);
        }
        Plant plant = new Plant(name);
        Plant savedPlant = plantRepository.save(plant);
        PlantResponse plantResponse = new PlantResponse(
                savedPlant.getId(),
                savedPlant.getName(),
                savedPlant.getLastWateredTime());
        return plantResponse;
    }

    public List<Plant> getAllPlants() {
        List<Plant> plants = plantRepository.findAll();

        return plants;
    }

    public Plant setLastWateredTime(Long id, LocalDateTime lastWateredTime) {
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new PlantNotFoundException(id));
        plant.setLastWateredTime(lastWateredTime);
        return plant;
    }

    public void removePlant(Long id) {
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new PlantNotFoundException(id));
        plantRepository.delete(plant);
    }
}
