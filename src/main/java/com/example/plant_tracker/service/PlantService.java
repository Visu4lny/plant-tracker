package com.example.plant_tracker.service;

import com.example.plant_tracker.dto.CreatePlantRequest;
import com.example.plant_tracker.dto.PlantResponse;
import com.example.plant_tracker.exception.PlantAlreadyExistsException;
import com.example.plant_tracker.exception.PlantNotFoundException;
import com.example.plant_tracker.model.Plant;
import com.example.plant_tracker.repository.PlantRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PlantService {

    private final PlantRepository plantRepository;

    public PlantService(PlantRepository plantRepository) {
        this.plantRepository = plantRepository;
    }


    public PlantResponse createPlant(CreatePlantRequest request) {
        if (plantRepository.existsByName(request.name())) {
            throw new PlantAlreadyExistsException(request.name());
        }
        Plant plant = new Plant(request.name());
        Plant savedPlant = plantRepository.save(plant);
        return new PlantResponse(
                savedPlant.getId(),
                savedPlant.getName(),
                savedPlant.getLastWateredTime());
    }

    public List<PlantResponse> getAllPlants(Sort.Direction direction, String property) {
        List<Plant> plants = plantRepository.findAll(Sort.by(direction, property));

        return plants.stream()
                .map(plant -> new PlantResponse(
                        plant.getId(),
                        plant.getName(),
                        plant.getLastWateredTime()
                ))
                .toList();
    }

    public PlantResponse updateLastWateredTime(UUID id, LocalDateTime lastWateredTime) {
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new PlantNotFoundException(id));
        plant.setLastWateredTime(lastWateredTime);

        return new PlantResponse(
                plant.getId(),
                plant.getName(),
                plant.getLastWateredTime()
        );
    }

    public void deletePlant(UUID id) {
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new PlantNotFoundException(id));
        plantRepository.delete(plant);
    }
}
