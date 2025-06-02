package com.example.plant_tracker.repository;

import com.example.plant_tracker.model.Plant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantRepository extends JpaRepository<Plant, Long> {

}
