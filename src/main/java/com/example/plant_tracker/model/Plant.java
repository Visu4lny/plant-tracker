package com.example.plant_tracker.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Plant {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;

    private LocalDateTime wateringTime;

    public Plant() {
    }

    public Plant(Long id, String name, LocalDateTime wateringTime) {
        this.id = id;
        this.name = name;
        this.wateringTime = wateringTime;
    }

    public Plant(String name) {
        this.name = name;
    }

    public Plant(String name, LocalDateTime wateringTime) {
        this.name = name;
        this.wateringTime = wateringTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getWateringTime() {
        return wateringTime;
    }

    public void setWateringTime(LocalDateTime wateringTime) {
        this.wateringTime = wateringTime;
    }
}
