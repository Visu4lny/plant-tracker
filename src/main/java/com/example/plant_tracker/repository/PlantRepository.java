package com.example.plant_tracker.repository;

import com.example.plant_tracker.model.Plant;
import com.example.plant_tracker.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlantRepository extends JpaRepository<Plant, UUID> {

    boolean existsByName(String name);

    List<Plant> findAllByUser(User user, Sort sort);

    Optional<Plant> findByIdAndUser(UUID id, User user);
}
