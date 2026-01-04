package com.example.plant_tracker.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
public class Plant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name;

    private Instant lastWateredAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Plant() {
    }

    public Plant(UUID id, String name, Instant lastWateredAt) {
        this.id = id;
        this.name = name;
        this.lastWateredAt = lastWateredAt;
    }

    public Plant(String name) {
        this.name = name;
    }

    public Plant(String name, Instant lastWateredAt) {
        this.name = name;
        this.lastWateredAt = lastWateredAt;
    }

    public Plant(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public Plant(String name, User user) {
        this.name = name;
        this.user = user;
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

    public Instant getLastWateredAt() {
        return lastWateredAt;
    }

    public void setLastWateredAt(Instant lastWateredAt) {
        this.lastWateredAt = lastWateredAt;
    }
}
