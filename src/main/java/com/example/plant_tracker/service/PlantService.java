package com.example.plant_tracker.service;

import com.example.plant_tracker.exception.PlantAlreadyExistsException;
import com.example.plant_tracker.model.Plant;
import com.example.plant_tracker.repository.PlantRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class PlantService {

    private PlantRepository plantRepository;

    public PlantService(PlantRepository plantRepository) {
        this.plantRepository = plantRepository;
    }


    public Plant addPlant(String name) {
        if (plantRepository.existsByName(name)) {
            throw new PlantAlreadyExistsException(name);
        }
        Plant plant = new Plant(name);
        Plant savedPlant = plantRepository.save(plant);
        return savedPlant;
    }
}
