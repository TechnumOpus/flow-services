package com.onified.distribute.controller;

import com.onified.distribute.dto.InventoryBufferDTO;
import com.onified.distribute.scheduler.BufferStatusUpdateScheduler;
import com.onified.distribute.service.dbm.InventoryBufferService;
//import com.onified.distribute.service.impl.DynamicBufferManagementServiceImpl;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/inventory-buffers")
@RequiredArgsConstructor
@Validated
public class InventoryBufferController {

    private final InventoryBufferService inventoryBufferService;
    private final BufferStatusUpdateScheduler bufferStatusUpdateScheduler;


    @PostMapping
    public ResponseEntity<InventoryBufferDTO> createInventoryBuffer(@Valid @RequestBody InventoryBufferDTO bufferDto) {
        log.info("Creating inventory buffer for product: {} at location: {}",
                bufferDto.getProductId(), bufferDto.getLocationId());

        InventoryBufferDTO createdBuffer = inventoryBufferService.createInventoryBuffer(bufferDto);
        return new ResponseEntity<>(createdBuffer, HttpStatus.CREATED);
    }

    @PutMapping("/{bufferId}")
    public ResponseEntity<InventoryBufferDTO> updateInventoryBuffer(
            @PathVariable String bufferId,
            @Valid @RequestBody InventoryBufferDTO bufferDto) {
        log.info("Updating inventory buffer: {}", bufferId);

        InventoryBufferDTO updatedBuffer = inventoryBufferService.updateInventoryBuffer(bufferId, bufferDto);
        return ResponseEntity.ok(updatedBuffer);
    }

    @GetMapping("/{bufferId}")
    public ResponseEntity<InventoryBufferDTO> getInventoryBufferById(@PathVariable String bufferId) {
        log.info("Fetching inventory buffer: {}", bufferId);

        InventoryBufferDTO buffer = inventoryBufferService.getInventoryBufferById(bufferId);
        return ResponseEntity.ok(buffer);
    }

    @GetMapping("/product/{productId}/location/{locationId}")
    public ResponseEntity<InventoryBufferDTO> getInventoryBufferByProductAndLocation(
            @PathVariable String productId,
            @PathVariable String locationId) {
        log.info("Fetching inventory buffer for product: {} at location: {}", productId, locationId);

        InventoryBufferDTO buffer = inventoryBufferService.getInventoryBufferByProductAndLocation(productId, locationId);
        return ResponseEntity.ok(buffer);
    }

    @GetMapping
    public ResponseEntity<Page<InventoryBufferDTO>> getAllInventoryBuffers(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("Fetching all inventory buffers - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<InventoryBufferDTO> buffers = inventoryBufferService.getAllInventoryBuffers(pageable);
        return ResponseEntity.ok(buffers);
    }

    @GetMapping("/zone/{currentZone}")
    public ResponseEntity<Page<InventoryBufferDTO>> getInventoryBuffersByCurrentZone(
            @PathVariable String currentZone,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        log.info("Fetching inventory buffers by current zone: {}", currentZone);

        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<InventoryBufferDTO> buffers = inventoryBufferService.getInventoryBuffersByCurrentZone(currentZone, pageable);
        return ResponseEntity.ok(buffers);
    }

    @GetMapping("/active")
    public ResponseEntity<Page<InventoryBufferDTO>> getActiveInventoryBuffers(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        log.info("Fetching active inventory buffers");

        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<InventoryBufferDTO> buffers = inventoryBufferService.getActiveInventoryBuffers(pageable);
        return ResponseEntity.ok(buffers);
    }



    @PutMapping("/calculate-zone/{bufferId}")
    public ResponseEntity<InventoryBufferDTO> calculateBufferZone(@PathVariable String bufferId) {
        log.info("Calculating buffer zone for buffer: {}", bufferId);

        InventoryBufferDTO buffer = inventoryBufferService.calculateBufferZone(bufferId);
        return ResponseEntity.ok(buffer);
    }

    @PutMapping("/inventory/{bufferId}")
    public ResponseEntity<InventoryBufferDTO> updateBufferInventory(
            @PathVariable String bufferId,
            @RequestParam Integer currentInventory,
            @RequestParam(defaultValue = "0") Integer inPipelineQty) {
        log.info("Updating buffer inventory for buffer: {} with current: {}, pipeline: {}",
                bufferId, currentInventory, inPipelineQty);

        InventoryBufferDTO buffer = inventoryBufferService.updateBufferInventory(bufferId, currentInventory, inPipelineQty);
        return ResponseEntity.ok(buffer);
    }

    @GetMapping("/zones")
    public ResponseEntity<List<InventoryBufferDTO>> getBuffersInZones(@RequestParam List<String> zones) {
        log.info("Fetching buffers in zones: {}", zones);

        List<InventoryBufferDTO> buffers = inventoryBufferService.getBuffersInZones(zones);
        return ResponseEntity.ok(buffers);
    }

    @DeleteMapping("/{bufferId}")
    public ResponseEntity<Void> deleteInventoryBuffer(@PathVariable String bufferId) {
        log.info("Deleting inventory buffer: {}", bufferId);

        inventoryBufferService.deleteInventoryBuffer(bufferId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkBufferExists(
            @RequestParam String productId,
            @RequestParam String locationId) {
        log.info("Checking if buffer exists for product: {} at location: {}", productId, locationId);

        boolean exists = inventoryBufferService.existsByProductAndLocation(productId, locationId);
        return ResponseEntity.ok(exists);
    }

    // New endpoint: Manually trigger buffer status update for all locations
    @PostMapping("/update-all")
    public ResponseEntity<Map<String, Object>> updateAllBuffers() {
        log.info("Manual trigger: updating buffer status for all locations");

        try {
            bufferStatusUpdateScheduler.updateBufferStatusForAllLocations();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Buffer status update completed successfully");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in manual buffer status update: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Buffer status update failed: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // New endpoint: Manually trigger buffer status update for a specific location
    @PostMapping("/update-location/{locationId}")
    public ResponseEntity<Map<String, Object>> updateBuffersForLocation(@PathVariable String locationId) {
        log.info("Manual trigger: updating buffer status for location: {}", locationId);

        try {
            BufferStatusUpdateScheduler.BufferUpdateResult result =
                    bufferStatusUpdateScheduler.updateBufferStatusForLocation(locationId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Buffer status update completed for location: " + locationId);
            response.put("locationId", locationId);
            response.put("processedCount", result.getProcessedCount());
            response.put("updatedCount", result.getUpdatedCount());
            response.put("errorCount", result.getErrorCount());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in manual buffer status update for location {}: {}", locationId, e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Buffer status update failed for location " + locationId + ": " + e.getMessage());
            response.put("locationId", locationId);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // New endpoint: Manually trigger critical buffer monitoring
    @GetMapping("/monitor-critical")
    public ResponseEntity<Map<String, Object>> monitorCriticalBuffers() {
        log.info("Manual trigger: monitoring critical buffers");

        try {
            bufferStatusUpdateScheduler.monitorCriticalBuffers();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Critical buffer monitoring completed successfully");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in manual critical buffer monitoring: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Critical buffer monitoring failed: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}