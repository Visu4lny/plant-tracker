package com.example.plant_tracker.controller;

import com.example.plant_tracker.dto.CreatePlantRequest;
import com.example.plant_tracker.dto.PlantResponse;
import com.example.plant_tracker.dto.UpdateLastWateredRequest;
import com.example.plant_tracker.service.PlantService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/plants")
public class PlantController {

    private final PlantService plantService;

    public PlantController(PlantService plantService) {
        this.plantService = plantService;
    }

    @PostMapping
    public ResponseEntity<PlantResponse> createPlant(
            @Valid @RequestBody CreatePlantRequest request
    ) {
        PlantResponse response = plantService.createPlant(request);
        return ResponseEntity.created(URI.create("/plants/" + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<PlantResponse>> getAllPlants(
            @RequestParam(defaultValue = "name,asc") String sort
    ) {
        String[] sortParams = sort.split(",");
        String property = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 ?
                Sort.Direction.fromString(sortParams[1]) :Sort.Direction.ASC;
        List<PlantResponse> response = plantService.getAllPlants(direction, property);
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping("/{id}/last-watered")
    public ResponseEntity<PlantResponse> updateLastWatered(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLastWateredRequest request
    ) {
        PlantResponse response = plantService.updateLastWateredTime(id, request.localDateTime());
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlant(@PathVariable UUID id) {
        plantService.deletePlant(id);
    }
}
