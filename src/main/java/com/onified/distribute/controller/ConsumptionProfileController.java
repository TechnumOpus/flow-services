package com.onified.distribute.controller;

import com.onified.distribute.dto.response.BufferConsumptionResponseDTO;
import com.onified.distribute.dto.ConsumptionProfileDTO;
import com.onified.distribute.service.consumption.ConsumptionProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

@Slf4j
@RestController
@RequestMapping("/api/v1/consumption-profiles")
@RequiredArgsConstructor
@Validated
public class ConsumptionProfileController {

    private final ConsumptionProfileService consumptionProfileService;

    @PostMapping
    public ResponseEntity<ConsumptionProfileDTO> createConsumptionProfile(@Valid @RequestBody ConsumptionProfileDTO profileDto) {
        log.info("Creating consumption profile for product: {} at location: {}",
                profileDto.getProductId(), profileDto.getLocationId());

        ConsumptionProfileDTO createdProfile = consumptionProfileService.createConsumptionProfile(profileDto);
        return new ResponseEntity<>(createdProfile, HttpStatus.CREATED);
    }

    @PutMapping("/{profileId}")
    public ResponseEntity<ConsumptionProfileDTO> updateConsumptionProfile(
            @PathVariable String profileId,
            @Valid @RequestBody ConsumptionProfileDTO profileDto) {
        log.info("Updating consumption profile: {}", profileId);

        ConsumptionProfileDTO updatedProfile = consumptionProfileService.updateConsumptionProfile(profileId, profileDto);
        return ResponseEntity.ok(updatedProfile);
    }

    @GetMapping("/{profileId}")
    public ResponseEntity<ConsumptionProfileDTO> getConsumptionProfileById(@PathVariable String profileId) {
        log.info("Fetching consumption profile: {}", profileId);

        ConsumptionProfileDTO profile = consumptionProfileService.getConsumptionProfileById(profileId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/product/{productId}/location/{locationId}")
    public ResponseEntity<ConsumptionProfileDTO> getConsumptionProfileByProductAndLocation(
            @PathVariable String productId,
            @PathVariable String locationId) {
        log.info("Fetching consumption profile for product: {} at location: {}", productId, locationId);

        ConsumptionProfileDTO profile = consumptionProfileService.getConsumptionProfileByProductAndLocation(productId, locationId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping
    public ResponseEntity<Page<ConsumptionProfileDTO>> getAllConsumptionProfiles(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "calculationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("Fetching all consumption profiles - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ConsumptionProfileDTO> profiles = consumptionProfileService.getAllConsumptionProfiles(pageable);
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/trend/{adcTrend}")
    public ResponseEntity<Page<ConsumptionProfileDTO>> getConsumptionProfilesByTrend(
            @PathVariable String adcTrend,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        log.info("Fetching consumption profiles by trend: {}", adcTrend);

        Pageable pageable = PageRequest.of(page, size, Sort.by("calculationDate").descending());
        Page<ConsumptionProfileDTO> profiles = consumptionProfileService.getConsumptionProfilesByTrend(adcTrend, pageable);
        return ResponseEntity.ok(profiles);
    }

    @PostMapping("/calculate")
    public ResponseEntity<ConsumptionProfileDTO> calculateConsumptionProfile(
            @RequestParam String productId,
            @RequestParam String locationId) {
        log.info("Calculating consumption profile for product: {} at location: {}", productId, locationId);

        ConsumptionProfileDTO profile = consumptionProfileService.calculateConsumptionProfile(productId, locationId);
        return new ResponseEntity<>(profile, HttpStatus.CREATED);
    }

    @PutMapping("/{profileId}/recalculate")
    public ResponseEntity<ConsumptionProfileDTO> recalculateProfile(@PathVariable String profileId) {
        log.info("Recalculating consumption profile: {}", profileId);

        ConsumptionProfileDTO profile = consumptionProfileService.recalculateProfile(profileId);
        return ResponseEntity.ok(profile);
    }

    @DeleteMapping("/{profileId}")
    public ResponseEntity<Void> deleteConsumptionProfile(@PathVariable String profileId) {
        log.info("Deleting consumption profile: {}", profileId);

        consumptionProfileService.deleteConsumptionProfile(profileId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkProfileExists(
            @RequestParam String productId,
            @RequestParam String locationId) {
        log.info("Checking if profile exists for product: {} at location: {}", productId, locationId);

        boolean exists = consumptionProfileService.existsByProductAndLocation(productId, locationId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/buffer-consumption")
    public ResponseEntity<BufferConsumptionResponseDTO> getBufferConsumptionData(
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String locationId,
            @RequestParam(required = false) String bufferZone,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "consumptionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("Fetching buffer and consumption data with filters - productId: {}, locationId: {}, bufferZone: {}, category: {}, page: {}, size: {}",
                productId, locationId, bufferZone, category, page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        BufferConsumptionResponseDTO response = consumptionProfileService.getBufferConsumptionData(
                productId, locationId, bufferZone, category, pageable);
        return ResponseEntity.ok(response);
    }
}
