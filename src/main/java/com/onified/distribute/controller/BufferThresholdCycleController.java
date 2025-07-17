package com.onified.distribute.controller;

import com.onified.distribute.dto.BufferThresholdCycleDTO;
import com.onified.distribute.dto.BufferThresholdUpdateDTO;
import com.onified.distribute.dto.ReviewCycleDTO;
import com.onified.distribute.dto.response.BufferOverriddenResponse;
import com.onified.distribute.service.dbm.BufferThresholdCycleService;
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
@RequestMapping("/api/v1/Dynamic-buffers")
@RequiredArgsConstructor
@Validated
public class BufferThresholdCycleController {

    private final BufferThresholdCycleService bufferThresholdCycleService;


    @GetMapping("/manual-overrides")
    public ResponseEntity<Page<BufferOverriddenResponse>> getAllManualBufferAdjustmentLogs(
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String locationId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "adjustmentDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Fetching manual buffer override logs - productId: {}, locationId: {}, page: {}, size: {}",
                productId, locationId, page, size);

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<BufferOverriddenResponse> overrides = bufferThresholdCycleService
                    .getAllManualBufferAdjustmentLogs(productId, locationId, pageable);

            log.info("Successfully fetched {} manual buffer override logs", overrides.getTotalElements());
            return ResponseEntity.ok(overrides);

        } catch (Exception e) {
            log.error("Error fetching manual buffer override logs with filters - productId: {}, locationId: {}",
                    productId, locationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/inventory-buffers
     * Fetch all inventory buffers with threshold and cycle information
     */
    @GetMapping
    public ResponseEntity<Page<BufferThresholdCycleDTO>> getInventoryBuffers(
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String locationId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("Fetching inventory buffers - productId: {}, locationId: {}, page: {}, size: {}",
                productId, locationId, page, size);

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<BufferThresholdCycleDTO> buffers = bufferThresholdCycleService
                    .getBufferThresholdCycles(productId, locationId, pageable);

            log.info("Successfully fetched {} inventory buffers", buffers.getTotalElements());
            return ResponseEntity.ok(buffers);

        } catch (Exception e) {
            log.error("Error fetching inventory buffers with filters - productId: {}, locationId: {}",
                    productId, locationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/inventory-buffers/{bufferId}
     * Get specific buffer threshold and cycle information
     */
    @GetMapping("/{bufferId}")
    public ResponseEntity<BufferThresholdCycleDTO> getInventoryBufferById(@PathVariable String bufferId) {
        log.info("Fetching inventory buffer by ID: {}", bufferId);

        try {
            BufferThresholdCycleDTO buffer = bufferThresholdCycleService.getBufferThresholdCycleById(bufferId);
            return ResponseEntity.ok(buffer);

        } catch (IllegalArgumentException e) {
            log.warn("Buffer not found with ID: {}", bufferId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error fetching inventory buffer by ID: {}", bufferId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT /api/inventory-buffers/{bufferId}
     * Update buffer threshold and cycle settings
     */
    @PutMapping("/{bufferId}")
    public ResponseEntity<Map<String, Object>> updateInventoryBuffer(
            @PathVariable String bufferId,
            @Valid @RequestBody BufferThresholdUpdateDTO updateDTO) {

        log.info("Updating inventory buffer: {} with data: {}", bufferId, updateDTO);

        try {
            // Validate threshold percentages
            if (updateDTO.getRedThresholdPct() != null && updateDTO.getYellowThresholdPct() != null) {
                if (updateDTO.getRedThresholdPct() + updateDTO.getYellowThresholdPct() > 100) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Invalid threshold configuration");
                    errorResponse.put("message", "Combined red and yellow thresholds cannot exceed 100%");
                    errorResponse.put("bufferId", bufferId);
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            }

            BufferThresholdCycleDTO updatedBuffer = bufferThresholdCycleService
                    .updateBufferThresholdCycle(bufferId, updateDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Buffer threshold and cycle updated successfully");
            response.put("bufferId", bufferId);
            response.put("data", updatedBuffer);
            response.put("timestamp", System.currentTimeMillis());

            log.info("Successfully updated inventory buffer: {}", bufferId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for buffer update - bufferId: {}, error: {}", bufferId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid request");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("bufferId", bufferId);
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            log.error("Error updating inventory buffer: {}", bufferId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            errorResponse.put("message", "Failed to update buffer threshold and cycle");
            errorResponse.put("bufferId", bufferId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * GET /api/inventory-buffers/review-cycles
     * Get all available review cycles for dropdown
     */
    @GetMapping("/review-cycles")
    public ResponseEntity<Map<String, Object>> getAvailableReviewCycles() {
        log.info("Fetching available review cycles");

        try {
            List<ReviewCycleDTO> reviewCycles = bufferThresholdCycleService.getAvailableReviewCycles();

            Map<String, Object> response = new HashMap<>();
            response.put("reviewCycles", reviewCycles);
            response.put("totalCount", reviewCycles.size());
            response.put("timestamp", System.currentTimeMillis());

            log.info("Successfully fetched {} review cycles", reviewCycles.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching available review cycles", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch review cycles");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * GET /api/inventory-buffers/summary
     * Get summary statistics for buffer thresholds and cycles
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getBufferSummary(
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String locationId) {

        log.info("Fetching buffer summary - productId: {}, locationId: {}", productId, locationId);

        try {
            // Get all buffers with filters
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
            Page<BufferThresholdCycleDTO> buffers = bufferThresholdCycleService
                    .getBufferThresholdCycles(productId, locationId, pageable);

            // Calculate summary statistics
            long totalBuffers = buffers.getTotalElements();
            long systemAutomatedBuffers = buffers.getContent().stream()
                    .filter(buffer -> "System".equals(buffer.getReviewAutomation()))
                    .filter(buffer -> "System".equals(buffer.getReviewAutomation()))
                    .count();
            long manualBuffers = totalBuffers - systemAutomatedBuffers;

            // Count buffers by zone
            Map<String, Long> zoneDistribution = buffers.getContent().stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            buffer -> buffer.getCurrentZone() != null ? buffer.getCurrentZone() : "Unknown",
                            java.util.stream.Collectors.counting()));

            // Count buffers by review cycle
            Map<String, Long> reviewCycleDistribution = buffers.getContent().stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            buffer -> buffer.getReviewCycleName() != null ? buffer.getReviewCycleName() : "No Cycle",
                            java.util.stream.Collectors.counting()));

            Map<String, Object> response = new HashMap<>();
            response.put("totalBuffers", totalBuffers);
            response.put("systemAutomatedBuffers", systemAutomatedBuffers);
            response.put("manualBuffers", manualBuffers);
            response.put("zoneDistribution", zoneDistribution);
            response.put("reviewCycleDistribution", reviewCycleDistribution);
            response.put("timestamp", System.currentTimeMillis());

            log.info("Successfully generated buffer summary - total: {}, automated: {}, manual: {}",
                    totalBuffers, systemAutomatedBuffers, manualBuffers);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error generating buffer summary", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to generate buffer summary");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * POST /api/inventory-buffers/{bufferId}/trigger-review
     * Trigger manual review for a specific buffer
     */
    @PostMapping("/{bufferId}/trigger-review")
    public ResponseEntity<Map<String, Object>> triggerBufferReview(@PathVariable String bufferId) {
        log.info("Triggering manual review for buffer: {}", bufferId);

        try {
            bufferThresholdCycleService.triggerBufferReview(bufferId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Buffer review triggered successfully");
            response.put("bufferId", bufferId);
            response.put("timestamp", System.currentTimeMillis());
            response.put("status", "SUCCESS");

            log.info("Successfully triggered review for buffer: {}", bufferId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Buffer not found for review trigger: {}", bufferId);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Buffer not found");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("bufferId", bufferId);
            errorResponse.put("status", "NOT_FOUND");
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error triggering review for buffer: {}", bufferId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to trigger buffer review");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("bufferId", bufferId);
            errorResponse.put("status", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}
