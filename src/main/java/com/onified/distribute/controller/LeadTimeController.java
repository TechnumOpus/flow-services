package com.onified.distribute.controller;

import com.onified.distribute.dto.LeadTimeDTO;
import com.onified.distribute.service.LeadTimeService;
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
@RequestMapping("/api/v1/lead-times")
@RequiredArgsConstructor
@Validated
public class LeadTimeController {

    private final LeadTimeService leadTimeService;

    @PostMapping
    public ResponseEntity<LeadTimeDTO> createLeadTime(@Valid @RequestBody LeadTimeDTO leadTimeDto) {
        log.info("Creating lead time for product: {} at location: {}",
                leadTimeDto.getProductId(), leadTimeDto.getLocationId());
        LeadTimeDTO createdLeadTime = leadTimeService.createLeadTime(leadTimeDto);
        return new ResponseEntity<>(createdLeadTime, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeadTimeDTO> updateLeadTime(
            @PathVariable String id,
            @Valid @RequestBody LeadTimeDTO leadTimeDto) {
        log.info("Updating lead time: {}", id);
        LeadTimeDTO updatedLeadTime = leadTimeService.updateLeadTime(id, leadTimeDto);
        return ResponseEntity.ok(updatedLeadTime);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeadTimeDTO> getLeadTimeById(@PathVariable String id) {
        log.info("Fetching lead time: {}", id);
        LeadTimeDTO leadTime = leadTimeService.getLeadTimeById(id);
        return ResponseEntity.ok(leadTime);
    }

    @GetMapping
    public ResponseEntity<Page<LeadTimeDTO>> getAllLeadTimes(Pageable pageable) {
        log.info("Fetching all lead times with pagination");
        Page<LeadTimeDTO> leadTimes = leadTimeService.getAllLeadTimes(pageable);
        return ResponseEntity.ok(leadTimes);
    }

    @GetMapping("/active")
    public ResponseEntity<Page<LeadTimeDTO>> getActiveLeadTimes(Pageable pageable) {
        log.info("Fetching active lead times with pagination");
        Page<LeadTimeDTO> leadTimes = leadTimeService.getActiveLeadTimes(pageable);
        return ResponseEntity.ok(leadTimes);
    }

    @GetMapping("/product/{productId}/location/{locationId}/active")
    public ResponseEntity<LeadTimeDTO> getActiveLeadTimeByProductAndLocation(
            @PathVariable String productId,
            @PathVariable String locationId) {
        log.info("Fetching active lead time for product: {} at location: {}", productId, locationId);
        LeadTimeDTO leadTime = leadTimeService.getActiveLeadTimeByProductAndLocation(productId, locationId);
        return ResponseEntity.ok(leadTime);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<LeadTimeDTO>> getLeadTimesByProduct(
            @PathVariable String productId,
            Pageable pageable) {
        log.info("Fetching lead times by product: {}", productId);
        Page<LeadTimeDTO> leadTimes = leadTimeService.getLeadTimesByProduct(productId, pageable);
        return ResponseEntity.ok(leadTimes);
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<Page<LeadTimeDTO>> getLeadTimesByLocation(
            @PathVariable String locationId,
            Pageable pageable) {
        log.info("Fetching lead times by location: {}", locationId);
        Page<LeadTimeDTO> leadTimes = leadTimeService.getLeadTimesByLocation(locationId, pageable);
        return ResponseEntity.ok(leadTimes);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<LeadTimeDTO>> getLeadTimesByProductAndLocationIds(
            @RequestParam List<String> productIds,
            @RequestParam List<String> locationIds) {
        log.info("Fetching lead times by product IDs: {} and location IDs: {}", productIds, locationIds);
        List<LeadTimeDTO> leadTimes = leadTimeService.getLeadTimesByProductAndLocationIds(productIds, locationIds);
        return ResponseEntity.ok(leadTimes);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<LeadTimeDTO> activateLeadTime(@PathVariable String id) {
        log.info("Activating lead time: {}", id);
        LeadTimeDTO leadTime = leadTimeService.activateLeadTime(id);
        return ResponseEntity.ok(leadTime);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<LeadTimeDTO> deactivateLeadTime(@PathVariable String id) {
        log.info("Deactivating lead time: {}", id);
        LeadTimeDTO leadTime = leadTimeService.deactivateLeadTime(id);
        return ResponseEntity.ok(leadTime);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeadTime(@PathVariable String id) {
        log.info("Deleting lead time: {}", id);
        leadTimeService.deleteLeadTime(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product/{productId}/location/{locationId}/total")
    public ResponseEntity<Double> calculateTotalLeadTime(
            @PathVariable String productId,
            @PathVariable String locationId) {
        log.info("Calculating total lead time for product: {} at location: {}", productId, locationId);
        Double totalLeadTime = leadTimeService.calculateTotalLeadTime(productId, locationId);
        return ResponseEntity.ok(totalLeadTime);
    }
}
