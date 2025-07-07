package com.onified.distribute.controller;

import com.onified.distribute.dto.ReplenishmentOverrideLogDTO;
import com.onified.distribute.dto.ReplenishmentQueueDTO;
import com.onified.distribute.repository.ConsumptionProfileRepository;
import com.onified.distribute.repository.LeadTimeRepository;
import com.onified.distribute.repository.ProductRepository;
import com.onified.distribute.service.ReplenishmentOverrideLogService;
import com.onified.distribute.service.ReplenishmentQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/replenishment-queue")
@RequiredArgsConstructor
public class ReplenishmentQueueController {

    private final ReplenishmentQueueService replenishmentQueueService;
    private final ReplenishmentOverrideLogService overrideLogService;
    private final ConsumptionProfileRepository consumptionProfileRepository;
    private final LeadTimeRepository leadTimeRepository;
    private final ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<Page<ReplenishmentQueueDTO>> getAllReplenishmentQueues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "priorityScore") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("Fetching all replenishment queues - page: {}, size: {}", page, size);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ReplenishmentQueueDTO> queues = replenishmentQueueService.getAllReplenishmentQueues(pageable);
        return ResponseEntity.ok(queues);
    }

    @GetMapping("/overrides")
    public ResponseEntity<Page<ReplenishmentOverrideLogDTO>> getAllOverrideLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String locationId) {
        log.info("Fetching override logs - productId: {}, locationId: {}, page: {}, size: {}", productId, locationId, page, size);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ReplenishmentOverrideLogDTO> overrideLogs;
        if (productId != null && locationId != null) {
            overrideLogs = overrideLogService.getOverrideLogsByProductIdAndLocationId(productId, locationId, pageable);
        } else if (productId != null) {
            overrideLogs = overrideLogService.getOverrideLogsByProductId(productId, pageable);
        } else if (locationId != null) {
            overrideLogs = overrideLogService.getOverrideLogsByLocationId(locationId, pageable);
        } else {
            overrideLogs = overrideLogService.getAllOverrideLogs(pageable);
        }
        return ResponseEntity.ok(overrideLogs);
    }

    @PostMapping
    public ResponseEntity<ReplenishmentQueueDTO> createQueueItem(@Valid @RequestBody ReplenishmentQueueDTO queueDTO) {
        log.info("Creating replenishment queue item with ID: {}", queueDTO.getQueueId());
        ReplenishmentQueueDTO createdQueueItem = replenishmentQueueService.createQueueItem(queueDTO);
        return new ResponseEntity<>(createdQueueItem, HttpStatus.CREATED);
    }

    @PutMapping("/{queueId}")
    public ResponseEntity<ReplenishmentQueueDTO> updateQueueItem(
            @PathVariable String queueId,
            @Valid @RequestBody ReplenishmentQueueDTO queueDTO) {
        log.info("Updating replenishment queue item: {}", queueId);
        ReplenishmentQueueDTO updatedQueueItem = replenishmentQueueService.updateQueueItem(queueId, queueDTO);
        return ResponseEntity.ok(updatedQueueItem);
    }

    @GetMapping("/{queueId}")
    public ResponseEntity<ReplenishmentQueueDTO> getQueueItemById(@PathVariable String queueId) {
        log.info("Fetching replenishment queue item: {}", queueId);
        ReplenishmentQueueDTO queueItem = replenishmentQueueService.getQueueItemById(queueId);
        return ResponseEntity.ok(queueItem);
    }

    @GetMapping("/by-status")
    public ResponseEntity<Page<ReplenishmentQueueDTO>> getQueueItemsByStatus(
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching queue items by status: {}", status);
        Pageable pageable = PageRequest.of(page, size);
        Page<ReplenishmentQueueDTO> queueItems = replenishmentQueueService.getQueueItemsByStatus(status, pageable);
        return ResponseEntity.ok(queueItems);
    }

    @GetMapping("/by-zone-and-status")
    public ResponseEntity<Page<ReplenishmentQueueDTO>> getQueueItemsByBufferZoneAndStatus(
            @RequestParam String bufferZone,
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching queue items by buffer zone: {} and status: {}", bufferZone, status);
        Pageable pageable = PageRequest.of(page, size);
        Page<ReplenishmentQueueDTO> queueItems = replenishmentQueueService.getQueueItemsByBufferZoneAndStatus(bufferZone, status, pageable);
        return ResponseEntity.ok(queueItems);
    }

    @GetMapping("/debug/data-check/{productId}/{locationId}")
    public ResponseEntity<Map<String, Object>> debugDataCheck(
            @PathVariable String productId,
            @PathVariable String locationId) {
        Map<String, Object> result = new HashMap<>();
        var consumptionProfile = consumptionProfileRepository.findByProductIdAndLocationId(productId, locationId);
        result.put("consumptionProfileExists", consumptionProfile.isPresent());
        if (consumptionProfile.isPresent()) {
            result.put("adcNormalized", consumptionProfile.get().getAdcNormalized());
        }
        var leadTime = leadTimeRepository.findByProductIdAndLocationIdAndIsActive(productId, locationId, true);
        result.put("leadTimeExists", leadTime.isPresent());
        if (leadTime.isPresent()) {
            result.put("bufferLeadTimeDays", leadTime.get().getBufferLeadTimeDays());
        }
        var product = productRepository.findByProductId(productId);
        result.put("productExists", product.isPresent());
        if (product.isPresent()) {
            result.put("moq", product.get().getMoq());
        }
        return ResponseEntity.ok(result);
    }
}
