package com.example.plant_tracker.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Plant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(unique = true, nullable = false)
    private String name;

    private LocalDateTime lastWateredTime;

    public Plant() {
    }

    public Plant(UUID id, String name, LocalDateTime wateringTime) {
        this.id = id;
        this.name = name;
        this.lastWateredTime = wateringTime;
    }

    public Plant(String name) {
        this.name = name;
    }

    public Plant(String name, LocalDateTime wateringTime) {
        this.name = name;
        this.lastWateredTime = wateringTime;
    }

    public Plant(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getLastWateredTime() {
        return lastWateredTime;
    }

    public void setLastWateredTime(LocalDateTime lastWateredTime) {
        this.lastWateredTime = lastWateredTime;
    }
}
