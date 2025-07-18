package com.onified.distribute.controller;

import com.onified.distribute.dto.SeasonalityAdjustmentDTO;
import com.onified.distribute.dto.SeasonalityMatrixResponseDTO;
import com.onified.distribute.service.masterdata.SeasonalityAdjustmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/seasonality-adjustments")
@RequiredArgsConstructor
@Validated
public class SeasonalityAdjustmentController {

    private final SeasonalityAdjustmentService seasonalityAdjustmentService;

    @PostMapping
    public ResponseEntity<SeasonalityAdjustmentDTO> createSeasonalityAdjustment(
            @Valid @RequestBody SeasonalityAdjustmentDTO adjustmentDto) {
        log.info("Creating seasonality adjustment for product: {} at location: {} for month: {}",
                adjustmentDto.getProductId(), adjustmentDto.getLocationId(), adjustmentDto.getMonth());
        SeasonalityAdjustmentDTO createdAdjustment = seasonalityAdjustmentService.createSeasonalityAdjustment(adjustmentDto);
        return new ResponseEntity<>(createdAdjustment, HttpStatus.CREATED);
    }

    @PutMapping("/{adjustmentId}")
    public ResponseEntity<SeasonalityAdjustmentDTO> updateSeasonalityAdjustment(
            @PathVariable String adjustmentId,
            @Valid @RequestBody SeasonalityAdjustmentDTO adjustmentDto) {
        log.info("Updating seasonality adjustment: {}", adjustmentId);
        SeasonalityAdjustmentDTO updatedAdjustment = seasonalityAdjustmentService.updateSeasonalityAdjustment(adjustmentId, adjustmentDto);
        return ResponseEntity.ok(updatedAdjustment);
    }

    @GetMapping("/{adjustmentId}")
    public ResponseEntity<SeasonalityAdjustmentDTO> getSeasonalityAdjustmentById(@PathVariable String adjustmentId) {
        log.info("Fetching seasonality adjustment: {}", adjustmentId);
        SeasonalityAdjustmentDTO adjustment = seasonalityAdjustmentService.getSeasonalityAdjustmentById(adjustmentId);
        return ResponseEntity.ok(adjustment);
    }

    @GetMapping
    public ResponseEntity<Page<SeasonalityAdjustmentDTO>> getAllSeasonalityAdjustments(Pageable pageable) {
        log.info("Fetching all seasonality adjustments with pagination");
        Page<SeasonalityAdjustmentDTO> adjustments = seasonalityAdjustmentService.getAllSeasonalityAdjustments(pageable);
        return ResponseEntity.ok(adjustments);
    }

    @GetMapping("/active")
    public ResponseEntity<Page<SeasonalityAdjustmentDTO>> getActiveSeasonalityAdjustments(Pageable pageable) {
        log.info("Fetching active seasonality adjustments with pagination");
        Page<SeasonalityAdjustmentDTO> adjustments = seasonalityAdjustmentService.getActiveSeasonalityAdjustments(pageable);
        return ResponseEntity.ok(adjustments);
    }

    @GetMapping("/month/{month}")
    public ResponseEntity<Page<SeasonalityAdjustmentDTO>> getSeasonalityAdjustmentsByMonth(
            @PathVariable @Min(1) @Max(12) Integer month,
            Pageable pageable) {
        log.info("Fetching seasonality adjustments by month: {}", month);
        Page<SeasonalityAdjustmentDTO> adjustments = seasonalityAdjustmentService.getSeasonalityAdjustmentsByMonth(month, pageable);
        return ResponseEntity.ok(adjustments);
    }

    @GetMapping("/product/{productId}/location/{locationId}")
    public ResponseEntity<List<SeasonalityAdjustmentDTO>> getSeasonalityAdjustmentsByProductAndLocation(
            @PathVariable String productId,
            @PathVariable String locationId) {
        log.info("Fetching seasonality adjustments for product: {} at location: {}", productId, locationId);
        List<SeasonalityAdjustmentDTO> adjustments = seasonalityAdjustmentService.getSeasonalityAdjustmentsByProductAndLocation(productId, locationId);
        return ResponseEntity.ok(adjustments);
    }

    @GetMapping("/product/{productId}/location/{locationId}/month/{month}/factor")
    public ResponseEntity<Double> getSeasonalityFactor(
            @PathVariable String productId,
            @PathVariable String locationId,
            @PathVariable @Min(1) @Max(12) Integer month) {
        log.info("Fetching seasonality factor for product: {} at location: {} for month: {}", productId, locationId, month);
        Double factor = seasonalityAdjustmentService.getSeasonalityFactor(productId, locationId, month);
        return ResponseEntity.ok(factor);
    }

    @PatchMapping("/{adjustmentId}/activate")
    public ResponseEntity<SeasonalityAdjustmentDTO> activateSeasonalityAdjustment(@PathVariable String adjustmentId) {
        log.info("Activating seasonality adjustment: {}", adjustmentId);
        SeasonalityAdjustmentDTO adjustment = seasonalityAdjustmentService.activateSeasonalityAdjustment(adjustmentId);
        return ResponseEntity.ok(adjustment);
    }

    @PatchMapping("/{adjustmentId}/deactivate")
    public ResponseEntity<SeasonalityAdjustmentDTO> deactivateSeasonalityAdjustment(@PathVariable String adjustmentId) {
        log.info("Deactivating seasonality adjustment: {}", adjustmentId);
        SeasonalityAdjustmentDTO adjustment = seasonalityAdjustmentService.deactivateSeasonalityAdjustment(adjustmentId);
        return ResponseEntity.ok(adjustment);
    }

    @DeleteMapping("/{adjustmentId}")
    public ResponseEntity<Void> deleteSeasonalityAdjustment(@PathVariable String adjustmentId) {
        log.info("Deleting seasonality adjustment: {}", adjustmentId);
        seasonalityAdjustmentService.deleteSeasonalityAdjustment(adjustmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/matrix")
    public ResponseEntity<Page<SeasonalityMatrixResponseDTO>> getSeasonalityMatrix(
            @RequestParam(defaultValue = "ALL") String type,
            @RequestParam(required = false) String locationId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String productId,
            @RequestParam(defaultValue = "2025") Integer year,
            @RequestParam(defaultValue = "true") Boolean isActive,
            Pageable pageable) {
        log.info("Fetching seasonality matrix with filters: type={}, locationId={}, category={}, productId={}, year={}, isActive={}",
                type, locationId, category, productId, year, isActive);
        Page<SeasonalityMatrixResponseDTO> adjustments = seasonalityAdjustmentService.getSeasonalityMatrix(
                type, locationId, category, productId, year, isActive, pageable);
        return ResponseEntity.ok(adjustments);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<SeasonalityAdjustmentDTO>> updateSeasonalityBulk(
            @Valid @RequestBody List<SeasonalityAdjustmentDTO> adjustments) {
        log.info("Updating seasonality adjustments in bulk for {} entries", adjustments.size());
        List<SeasonalityAdjustmentDTO> updatedAdjustments = seasonalityAdjustmentService.updateSeasonalityBulk(adjustments);
        return new ResponseEntity<>(updatedAdjustments, HttpStatus.OK);
    }
}
