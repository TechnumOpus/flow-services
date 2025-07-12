package com.onified.distribute.controller;

import com.onified.distribute.dto.*;
import com.onified.distribute.service.*;
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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/buffer-calculation")
@RequiredArgsConstructor
public class DynamicBufferController {

    private final BufferCalculationService bufferCalculationService;
    private final BufferReviewService bufferReviewService;

    @GetMapping("/review")
    public ResponseEntity<Page<BufferReviewDTO>> getBufferReviewData(
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String locationId,
            @RequestParam(required = false) String currentZone,
            @RequestParam(required = false) Boolean dueForReview,
            Pageable pageable) {
        Page<BufferReviewDTO> reviewData = bufferReviewService.getBufferReviewData(productId, locationId, currentZone, dueForReview, pageable);
        return new ResponseEntity<>(reviewData, HttpStatus.OK);
    }

    @GetMapping("/review/{bufferId}")
    public ResponseEntity<BufferReviewDTO> getBufferReviewById(@PathVariable String bufferId) {
        BufferReviewDTO reviewData = bufferReviewService.getBufferReviewById(bufferId);
        return new ResponseEntity<>(reviewData, HttpStatus.OK);
    }

    @PutMapping("/{bufferId}/adjust")
    public ResponseEntity<BufferAdjustmentLogDTO> adjustBuffer(@PathVariable String bufferId, @RequestBody BufferAdjustmentRequestDTO request) {
        request.setBufferId(bufferId); // Ensure bufferId is set in the request
        BufferAdjustmentLogDTO adjustmentLog = bufferReviewService.adjustBuffer(request);
        return new ResponseEntity<>(adjustmentLog, HttpStatus.OK);
    }
    /**
     * Calculate buffer quantity for a specific product and location
     */
    @PostMapping("/calculate")
    public ResponseEntity<BufferCalculationResponseDTO> calculateBufferQuantity(
            @Valid @RequestBody BufferCalculationRequestDTO request) {
        log.info("Calculating buffer quantity for product: {} at location: {} with baseADC: {} and safetyFactor: {}%",
                request.getProductId(), request.getLocationId(), request.getBaseADC(), request.getSafetyFactor());

        try {
            BufferCalculationResponseDTO response = bufferCalculationService.calculateBufferQuantity(request);

            HttpStatus status = switch (response.getCalculationStatus()) {
                case "SUCCESS" -> HttpStatus.OK;
                case "PARTIAL_DATA" -> HttpStatus.PARTIAL_CONTENT;
                case "ERROR" -> HttpStatus.BAD_REQUEST;
                default -> HttpStatus.OK;
            };

            return new ResponseEntity<>(response, status);
        } catch (Exception e) {
            log.error("Error in buffer calculation API for product: {} at location: {}",
                    request.getProductId(), request.getLocationId(), e);

            BufferCalculationResponseDTO errorResponse = BufferCalculationResponseDTO.builder()
                    .productId(request.getProductId())
                    .locationId(request.getLocationId())
                    .baseADC(request.getBaseADC())
                    .safetyFactor(request.getSafetyFactor())
                    .calculationStatus("ERROR")
                    .message("Internal server error: " + e.getMessage())
                    .build();

            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Calculate buffer quantities for multiple products at a specific location
     */
    @GetMapping("/location/{locationId}")
    public ResponseEntity<Map<String, Object>> calculateBufferQuantitiesForLocation(
            @PathVariable String locationId,
            @RequestParam @Pattern(regexp = "^(7adc|14adc|30adc)$", message = "Base ADC must be one of: 7adc, 14adc, 30adc") String baseADC,
            @RequestParam @DecimalMin(value = "0.0", message = "Safety factor must be non-negative") Double safetyFactor,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("Calculating buffer quantities for location: {} with baseADC: {} and safetyFactor: {}%",
                locationId, baseADC, safetyFactor);

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            List<BufferCalculationResponseDTO> calculations =
                    bufferCalculationService.calculateBufferQuantitiesForLocation(locationId, baseADC, safetyFactor, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("locationId", locationId);
            response.put("baseADC", baseADC);
            response.put("safetyFactor", safetyFactor);
            response.put("calculations", calculations);
            response.put("totalCalculations", calculations.size());
            response.put("page", page);
            response.put("size", size);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calculating buffer quantities for location: {}", locationId, e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to calculate buffer quantities for location");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("locationId", locationId);
            errorResponse.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Calculate buffer quantities for a specific product across multiple locations
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<Map<String, Object>> calculateBufferQuantitiesForProduct(
            @PathVariable String productId,
            @RequestParam @Pattern(regexp = "^(7adc|14adc|30adc)$", message = "Base ADC must be one of: 7adc, 14adc, 30adc") String baseADC,
            @RequestParam @DecimalMin(value = "0.0", message = "Safety factor must be non-negative") Double safetyFactor,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "locationId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("Calculating buffer quantities for product: {} with baseADC: {} and safetyFactor: {}%",
                productId, baseADC, safetyFactor);

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            List<BufferCalculationResponseDTO> calculations =
                    bufferCalculationService.calculateBufferQuantitiesForProduct(productId, baseADC, safetyFactor, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("productId", productId);
            response.put("baseADC", baseADC);
            response.put("safetyFactor", safetyFactor);
            response.put("calculations", calculations);
            response.put("totalCalculations", calculations.size());
            response.put("page", page);
            response.put("size", size);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calculating buffer quantities for product: {}", productId, e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to calculate buffer quantities for product");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("productId", productId);
            errorResponse.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Quick calculation endpoint with URL parameters
     */
    @GetMapping("/quick-calculate")
    public ResponseEntity<BufferCalculationResponseDTO> quickCalculateBufferQuantity(
            @RequestParam String productId,
            @RequestParam String locationId,
            @RequestParam @Pattern(regexp = "^(7adc|14adc|30adc)$", message = "Base ADC must be one of: 7adc, 14adc, 30adc") String baseADC,
            @RequestParam @DecimalMin(value = "0.0", message = "Safety factor must be non-negative") Double safetyFactor) {

        log.info("Quick calculating buffer quantity for product: {} at location: {} with baseADC: {} and safetyFactor: {}%",
                productId, locationId, baseADC, safetyFactor);

        try {
            BufferCalculationRequestDTO request = new BufferCalculationRequestDTO();
            request.setProductId(productId);
            request.setLocationId(locationId);
            request.setBaseADC(baseADC);
            request.setSafetyFactor(safetyFactor);

            BufferCalculationResponseDTO response = bufferCalculationService.calculateBufferQuantity(request);

            HttpStatus status = switch (response.getCalculationStatus()) {
                case "SUCCESS" -> HttpStatus.OK;
                case "PARTIAL_DATA" -> HttpStatus.PARTIAL_CONTENT;
                case "ERROR" -> HttpStatus.BAD_REQUEST;
                default -> HttpStatus.OK;
            };

            return new ResponseEntity<>(response, status);
        } catch (Exception e) {
            log.error("Error in quick buffer calculation for product: {} at location: {}",
                    productId, locationId, e);

            BufferCalculationResponseDTO errorResponse = BufferCalculationResponseDTO.builder()
                    .productId(productId)
                    .locationId(locationId)
                    .baseADC(baseADC)
                    .safetyFactor(safetyFactor)
                    .calculationStatus("ERROR")
                    .message("Internal server error: " + e.getMessage())
                    .build();

            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Check if buffer calculation is possible for given product and location
     */
    @GetMapping("/can-calculate")
    public ResponseEntity<Map<String, Object>> canCalculateBuffer(
            @RequestParam String productId,
            @RequestParam String locationId) {

        log.info("Checking if buffer calculation is possible for product: {} at location: {}", productId, locationId);

        try {
            boolean canCalculate = bufferCalculationService.canCalculateBuffer(productId, locationId);

            Map<String, Object> response = new HashMap<>();
            response.put("productId", productId);
            response.put("locationId", locationId);
            response.put("canCalculate", canCalculate);
            response.put("message", canCalculate ?
                    "Buffer calculation is possible" :
                    "Missing required data for buffer calculation");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error checking buffer calculation possibility for product: {} at location: {}",
                    productId, locationId, e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to check buffer calculation possibility");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("productId", productId);
            errorResponse.put("locationId", locationId);
            errorResponse.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Batch calculation endpoint for multiple product-location combinations
     */
    @PostMapping("/batch-calculate")
    public ResponseEntity<Map<String, Object>> batchCalculateBufferQuantities(
            @Valid @RequestBody List<BufferCalculationRequestDTO> requests) {

        log.info("Batch calculating buffer quantities for {} requests", requests.size());

        try {
            List<BufferCalculationResponseDTO> responses = requests.stream()
                    .map(bufferCalculationService::calculateBufferQuantity)
                    .toList();

            long successCount = responses.stream()
                    .filter(r -> "SUCCESS".equals(r.getCalculationStatus()))
                    .count();

            long partialCount = responses.stream()
                    .filter(r -> "PARTIAL_DATA".equals(r.getCalculationStatus()))
                    .count();

            long errorCount = responses.stream()
                    .filter(r -> "ERROR".equals(r.getCalculationStatus()))
                    .count();

            Map<String, Object> response = new HashMap<>();
            response.put("calculations", responses);
            response.put("totalRequests", requests.size());
            response.put("successCount", successCount);
            response.put("partialDataCount", partialCount);
            response.put("errorCount", errorCount);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in batch buffer calculation", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Batch buffer calculation failed");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("totalRequests", requests.size());
            errorResponse.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


}
