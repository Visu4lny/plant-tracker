package com.example.plant_tracker.repository;

import com.example.plant_tracker.model.Plant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlantRepository extends JpaRepository<Plant, UUID> {

    boolean existsByName(String name);
}
