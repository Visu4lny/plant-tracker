package com.example.plant_tracker.service;

import com.example.plant_tracker.dto.CreatePlantRequest;
import com.example.plant_tracker.dto.PlantResponse;
import com.example.plant_tracker.exception.PlantAlreadyExistsException;
import com.example.plant_tracker.exception.PlantNotFoundException;
import com.example.plant_tracker.model.Plant;
import com.example.plant_tracker.repository.PlantRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
        PlantResponse plantResponse = new PlantResponse(
                savedPlant.getId(),
                savedPlant.getName(),
                savedPlant.getLastWateredTime());
        return plantResponse;
    }

    public List<PlantResponse> getAllPlants() {
        List<Plant> plants = plantRepository.findAll();

        List<PlantResponse> response = plants.stream()
                .map(plant -> new PlantResponse(
                        plant.getId(),
                        plant.getName(),
                        plant.getLastWateredTime()
                ))
                .collect(Collectors.toList());

        return response;
    }

    public PlantResponse updateLastWateredTime(Long id, LocalDateTime lastWateredTime) {
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new PlantNotFoundException(id));
        plant.setLastWateredTime(lastWateredTime);

        PlantResponse response = new PlantResponse(
                plant.getId(),
                plant.getName(),
                plant.getLastWateredTime()
        );
        return response;
    }

    public void deletePlant(Long id) {
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new PlantNotFoundException(id));
        plantRepository.delete(plant);
    }
}
