package com.onified.distribute.controller;

import com.onified.distribute.dto.LocationDTO;
import com.onified.distribute.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Validated
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<LocationDTO> createLocation(@Valid @RequestBody LocationDTO locationDto) {
        log.info("Creating location: {}", locationDto.getName());
        LocationDTO createdLocation = locationService.createLocation(locationDto);
        return new ResponseEntity<>(createdLocation, HttpStatus.CREATED);
    }

    @PutMapping("/{locationId}")
    public ResponseEntity<LocationDTO> updateLocation(
            @PathVariable String locationId,
            @Valid @RequestBody LocationDTO locationDto) {
        log.info("Updating location: {}", locationId);
        LocationDTO updatedLocation = locationService.updateLocation(locationId, locationDto);
        return ResponseEntity.ok(updatedLocation);
    }

    @GetMapping("/{locationId}")
    public ResponseEntity<LocationDTO> getLocationById(@PathVariable String locationId) {
        log.info("Fetching location: {}", locationId);
        LocationDTO location = locationService.getLocationById(locationId);
        return ResponseEntity.ok(location);
    }

    @GetMapping
    public ResponseEntity<Page<LocationDTO>> getAllLocations(Pageable pageable) {
        log.info("Fetching all locations with pagination");
        Page<LocationDTO> locations = locationService.getAllLocations(pageable);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/active")
    public ResponseEntity<Page<LocationDTO>> getActiveLocations(Pageable pageable) {
        log.info("Fetching active locations with pagination");
        Page<LocationDTO> locations = locationService.getActiveLocations(pageable);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<Page<LocationDTO>> getLocationsByType(
            @PathVariable String type,
            Pageable pageable) {
        log.info("Fetching locations by type: {}", type);
        Page<LocationDTO> locations = locationService.getLocationsByType(type, pageable);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/region/{region}")
    public ResponseEntity<Page<LocationDTO>> getLocationsByRegion(
            @PathVariable String region,
            Pageable pageable) {
        log.info("Fetching locations by region: {}", region);
        Page<LocationDTO> locations = locationService.getLocationsByRegion(region, pageable);
        return ResponseEntity.ok(locations);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<LocationDTO>> getLocationsByIds(@RequestBody List<String> locationIds) {
        log.info("Fetching locations by IDs: {}", locationIds);
        List<LocationDTO    > locations = locationService.getLocationsByIds(locationIds);
        return ResponseEntity.ok(locations);
    }

    @PatchMapping("/{locationId}/activate")
    public ResponseEntity<LocationDTO> activateLocation(@PathVariable String locationId) {
        log.info("Activating location: {}", locationId);
        LocationDTO location = locationService.activateLocation(locationId);
        return ResponseEntity.ok(location);
    }

    @PatchMapping("/{locationId}/deactivate")
    public ResponseEntity<LocationDTO> deactivateLocation(@PathVariable String locationId) {
        log.info("Deactivating location: {}", locationId);
        LocationDTO  location = locationService.deactivateLocation(locationId);
        return ResponseEntity.ok(location);
    }

    @DeleteMapping("/{locationId}")
    public ResponseEntity<Void> deleteLocation(@PathVariable String locationId) {
        log.info("Deleting location: {}", locationId);
        locationService.deleteLocation(locationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{locationId}/exists")
    public ResponseEntity<Boolean> existsByLocationId(@PathVariable String locationId) {
        boolean exists = locationService.existsByLocationId(locationId);
        return ResponseEntity.ok(exists);
    }
}
