package com.example.plant_tracker.controller;

import com.example.plant_tracker.dto.CreatePlantRequest;
import com.example.plant_tracker.dto.PlantResponse;
import com.example.plant_tracker.dto.UpdateLastWateredRequest;
import com.example.plant_tracker.service.PlantService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/plants")
public class PlantController {

    public static final Logger log = LoggerFactory.getLogger(PlantController.class);

    private final PlantService plantService;

    public PlantController(PlantService plantService) {
        this.plantService = plantService;
    }

    @PostMapping
    public ResponseEntity<PlantResponse> createPlant(
            @Valid @RequestBody CreatePlantRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        log.debug("Creating plant for user: {}", email);

        PlantResponse response = plantService.createPlant(request, email);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PlantResponse>> getAllPlants(
            @RequestParam(defaultValue = "name,asc") String sort,
            Authentication authentication
    ) {
        String email = authentication.getName();
        log.debug("Fetching plants for user: {} with sort: {}", email, sort);

        String[] sortParams = sort.split(",");
        String property = sortParams[0];

        Sort.Direction direction = sortParams.length > 1
                ? Sort.Direction.fromString(sortParams[1])
                : Sort.Direction.ASC;

        List<PlantResponse> response = plantService.getUserPlants(direction, property, email);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/last-watered")
    public ResponseEntity<PlantResponse> updateLastWatered(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        String email = authentication.getName();
        log.debug("Updating last watered time for plant: {} by user: {}", id, email);

        PlantResponse response = plantService.updateLastWateredTime(id, email);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlant(
            @PathVariable UUID id,
            Authentication authentication
            ) {
        String email = authentication.getName();
        log.debug("Deleting plant: {} for user {}", id, email);
        plantService.deletePlant(id, email);
    }
}
