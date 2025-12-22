package com.example.plant_tracker.service;

import com.example.plant_tracker.dto.CreatePlantRequest;
import com.example.plant_tracker.dto.PlantResponse;
import com.example.plant_tracker.exception.PlantAlreadyExistsException;
import com.example.plant_tracker.exception.PlantNotFoundException;
import com.example.plant_tracker.model.Plant;
import com.example.plant_tracker.model.User;
import com.example.plant_tracker.repository.PlantRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PlantService {

    private final PlantRepository plantRepository;

    private final UserService userService;
    public PlantService(PlantRepository plantRepository, UserService userService) {
        this.plantRepository = plantRepository;
        this.userService = userService;
    }


    public PlantResponse createPlant(CreatePlantRequest request, String email) {
        if (plantRepository.existsByName(request.name())) {
            throw new PlantAlreadyExistsException(request.name());
        }
        User user = userService.findByEmail(email);
        Plant plant = new Plant(request.name(), user);
        Plant savedPlant = plantRepository.save(plant);
        return new PlantResponse(
                savedPlant.getId(),
                savedPlant.getName(),
                savedPlant.getLastWateredTime());
    }

    public List<PlantResponse> getUserPlants(Sort.Direction direction, String property, String email) {
        User user = userService.findByEmail(email);
        List<Plant> plants = plantRepository.findAllByUser(user, Sort.by(direction, property));

        return plants.stream()
                .map(plant -> new PlantResponse(
                        plant.getId(),
                        plant.getName(),
                        plant.getLastWateredTime()
                ))
                .toList();
    }

    public PlantResponse updateLastWateredTime(UUID id, String email) {
        User user = userService.findByEmail(email);

        Plant plant = plantRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new PlantNotFoundException(id));
        plant.setLastWateredTime(Instant.now());

        return new PlantResponse(
                plant.getId(),
                plant.getName(),
                plant.getLastWateredTime()
        );
    }

    public void deletePlant(UUID id, String email) {
        User user = userService.findByEmail(email);

        Plant plant = plantRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new PlantNotFoundException(id));
        plantRepository.delete(plant);
    }
}
