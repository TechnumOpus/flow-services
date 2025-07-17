package com.onified.distribute.scheduler;

import com.onified.distribute.entity.InventoryBuffer;
import com.onified.distribute.entity.InventoryOrderPipeline;
import com.onified.distribute.repository.DailyConsumptionLogRepository;
import com.onified.distribute.repository.InventoryBufferRepository;
import com.onified.distribute.repository.InventoryOrderPipelineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BufferStatusUpdateScheduler {

    private final InventoryBufferRepository inventoryBufferRepository;
    private final InventoryOrderPipelineRepository inventoryOrderPipelineRepository;
    private final DailyConsumptionLogRepository dailyConsumptionLogRepository;

    // Run every hour during business hours (9 AM to 6 PM)
    @Scheduled(cron = "30 37 17 * * MON-FRI")
    @Transactional
    public void updateBufferStatusForAllLocations() {
        log.info("Starting scheduled buffer status update for all locations");
        try {
            Set<String> locationIds = getAllActiveLocationIds();
            log.info("Found {} unique locations with active buffers", locationIds.size());

            int totalProcessed = 0;
            int totalUpdated = 0;
            int totalErrors = 0;

            for (String locationId : locationIds) {
                try {
                    BufferUpdateResult result = updateBufferStatusForLocation(locationId);
                    totalProcessed += result.getProcessedCount();
                    totalUpdated += result.getUpdatedCount();
                    totalErrors += result.getErrorCount();

                    log.info("Location {}: Processed={}, Updated={}, Errors={}",
                            locationId, result.getProcessedCount(), result.getUpdatedCount(), result.getErrorCount());
                } catch (Exception e) {
                    log.error("Failed to update buffer status for location: {}, error: {}", locationId, e.getMessage(), e);
                    totalErrors++;
                }
            }

            log.info("Completed scheduled buffer status update - Total Processed: {}, Updated: {}, Errors: {}",
                    totalProcessed, totalUpdated, totalErrors);
        } catch (Exception e) {
            log.error("Error in scheduled buffer status update: {}", e.getMessage(), e);
        }
    }

    // Run every 4 hours to check for critical buffers
    @Scheduled(fixedRate = 14400000) // 4 hours in milliseconds
    @Transactional(readOnly = true)
    public void monitorCriticalBuffers() {
        log.info("Starting critical buffer monitoring");
        try {
            List<String> criticalZones = Arrays.asList("RED", "CRITICAL");
            Pageable pageable = PageRequest.of(0, 1000);
            Page<InventoryBuffer> criticalBuffers = inventoryBufferRepository.findByCurrentZoneIn(criticalZones, pageable);

            if (criticalBuffers.hasContent()) {
                log.warn("Found {} critical buffers requiring attention", criticalBuffers.getTotalElements());

                Map<String, List<InventoryBuffer>> buffersByLocation = criticalBuffers.getContent()
                        .stream()
                        .collect(Collectors.groupingBy(InventoryBuffer::getLocationId));

                buffersByLocation.forEach((locationId, buffers) -> {
                    log.warn("Location {}: {} critical buffers - Products: {}",
                            locationId,
                            buffers.size(),
                            buffers.stream().map(InventoryBuffer::getProductId).collect(Collectors.toList()));
                });
            } else {
                log.info("No critical buffers found - all buffers are in healthy zones");
            }
        } catch (Exception e) {
            log.error("Error in critical buffer monitoring: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public BufferUpdateResult updateBufferStatusForLocation(String locationId) {
        log.info("Updating buffer status for location: {}", locationId);
        BufferUpdateResult result = new BufferUpdateResult();

        try {
            Page<InventoryBuffer> buffersPage = inventoryBufferRepository
                    .findActiveBuffersByLocationId(locationId, PageRequest.of(0, 1000));
            List<InventoryBuffer> buffers = buffersPage.getContent();

            log.debug("Found {} active buffers for location: {}", buffers.size(), locationId);

            if (buffers.isEmpty()) {
                log.info("No active buffers found for location: {}", locationId);
                return result;
            }

            // Get pipeline quantities for all products at this location
            Map<String, Integer> pipelineQuantities = calculatePipelineQuantities(locationId);

            // Process each buffer
            for (InventoryBuffer buffer : buffers) {
                try {
                    boolean updated = updateBufferMetrics(buffer, pipelineQuantities);
                    result.incrementProcessed();
                    if (updated) {
                        result.incrementUpdated();
                    }
                } catch (Exception e) {
                    log.error("Error updating buffer {}: {}", buffer.getBufferId(), e.getMessage(), e);
                    result.incrementError();
                }
            }

            log.info("Completed buffer status update for location: {} - Processed: {}, Updated: {}, Errors: {}",
                    locationId, result.getProcessedCount(), result.getUpdatedCount(), result.getErrorCount());
        } catch (Exception e) {
            log.error("Error updating buffer status for location {}: {}", locationId, e.getMessage(), e);
            result.incrementError();
        }

        return result;
    }

    private Map<String, Integer> calculatePipelineQuantities(String locationId) {
        log.debug("Starting pipeline calculation for location: {}", locationId);
        List<String> pipelineStatuses = Arrays.asList("CONFIRMED", "SHIPPED", "IN_TRANSIT","PROCESSED");
        Map<String, Integer> pipelineQuantities = new HashMap<>();

        for (String status : pipelineStatuses) {
            try {
                log.debug("Checking pipeline orders with status: {} for location: {}", status, locationId);
                Page<InventoryOrderPipeline> ordersPage = inventoryOrderPipelineRepository
                        .findByStatusAndLocationId(status, locationId, PageRequest.of(0, 1000));

                log.debug("Found {} orders with status: {} for location: {}",
                        ordersPage.getContent().size(), status, locationId);

                ordersPage.getContent().forEach(order -> {
                    String productId = order.getProductId();
                    Integer pendingQty = order.getPendingQty() != null ? order.getPendingQty() : 0;

                    if (pendingQty > 0) {
                        pipelineQuantities.merge(productId, pendingQty, Integer::sum);
                        log.debug("Added to pipeline - Order: {}, Product: {}, Qty: {}, Total for product: {}",
                                order.getOrderId(), productId, pendingQty, pipelineQuantities.get(productId));
                    }
                });
            } catch (Exception e) {
                log.error("Error calculating pipeline quantities for status {} at location {}: {}",
                        status, locationId, e.getMessage(), e);
            }
        }

        log.info("Final calculated pipeline quantities for location {}: {}", locationId, pipelineQuantities);
        return pipelineQuantities;
    }

    /**
     * Update metrics for a single buffer with corrected inventory calculation
     */
    private boolean updateBufferMetrics(InventoryBuffer buffer, Map<String, Integer> pipelineQuantities) {
        String productId = buffer.getProductId();
        String locationId = buffer.getLocationId();
        String bufferId = buffer.getBufferId();

        log.debug("Starting buffer metrics update for buffer: {}, product: {}", bufferId, productId);

        try {
            // Store original values for comparison
            String originalZone = buffer.getCurrentZone();
            Integer originalConsecutiveDays = buffer.getConsecutiveZoneDays() != null ? buffer.getConsecutiveZoneDays() : 0;
            Integer originalPipelineQty = buffer.getInPipelineQty();
            Integer originalCurrentInventory = buffer.getCurrentInventory();
            Integer originalNetAvailableQty = buffer.getNetAvailableQty();
            Double originalBufferConsumedPct = buffer.getBufferConsumedPct();

            // STEP 1: Calculate current inventory = buffer_units - yesterday's consumption
            Integer currentInventory = calculateCurrentInventory(buffer);
            buffer.setCurrentInventory(currentInventory);

            log.debug("Updated current_inventory for buffer {}: {} -> {} (buffer_units - yesterday's consumption)",
                    bufferId, originalCurrentInventory, currentInventory);

            // STEP 2: Get pipeline quantity for this product
            Integer pipelineQty = pipelineQuantities.getOrDefault(productId, 0);
            buffer.setInPipelineQty(pipelineQty);

            log.debug("Updated in_pipeline_qty for buffer {}: {} -> {}",
                    bufferId, originalPipelineQty, pipelineQty);

            // STEP 3: Calculate net available quantity = current_inventory + pipeline_qty
            Integer netAvailableQty = currentInventory + pipelineQty;
            buffer.setNetAvailableQty(netAvailableQty);

            log.debug("Updated net_available_qty for buffer {}: {} -> {} (current_inventory: {} + pipeline_qty: {})",
                    bufferId, originalNetAvailableQty, netAvailableQty, currentInventory, pipelineQty);

            // STEP 4: Calculate buffer consumed percentage
            Double bufferConsumedPct = calculateBufferConsumedPercentage(buffer, netAvailableQty);
            buffer.setBufferConsumedPct(bufferConsumedPct);

            log.debug("Updated buffer_consumed_pct for buffer {}: {}% -> {}%",
                    bufferId,
                    originalBufferConsumedPct != null ? String.format("%.2f", originalBufferConsumedPct) : "null",
                    String.format("%.2f", bufferConsumedPct));

            // STEP 5: Determine current zone based on consumed percentage
            String newZone = determineBufferZone(buffer, bufferConsumedPct);
            buffer.setCurrentZone(newZone);

            // STEP 6: Update consecutive zone days
            updateConsecutiveZoneDays(buffer, originalZone, newZone, originalConsecutiveDays);

            // STEP 7: Update timestamps
            buffer.setUpdatedAt(LocalDateTime.now());

            // STEP 8: Save the buffer
            inventoryBufferRepository.save(buffer);

            // Check if any significant changes occurred
            boolean hasSignificantChanges = !Objects.equals(originalZone, newZone) ||
                    !Objects.equals(originalCurrentInventory, currentInventory) ||
                    !Objects.equals(originalPipelineQty, pipelineQty) ||
                    Math.abs((originalBufferConsumedPct != null ? originalBufferConsumedPct : 0.0) - bufferConsumedPct) > 1.0;

            if (hasSignificantChanges) {
                log.info("Buffer {} updated - Zone: {} -> {}, Current Inventory: {} -> {}, Pipeline: {} -> {}, Consumed: {}% -> {}%",
                        bufferId, originalZone, newZone, originalCurrentInventory, currentInventory,
                        originalPipelineQty, pipelineQty,
                        originalBufferConsumedPct != null ? String.format("%.2f", originalBufferConsumedPct) : "null",
                        String.format("%.2f", bufferConsumedPct));
            }

            return hasSignificantChanges;

        } catch (Exception e) {
            log.error("Error updating buffer metrics for buffer {}: {}", bufferId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Calculate current inventory = buffer_units - yesterday's consumption
     */
    private Integer calculateCurrentInventory(InventoryBuffer buffer) {
        String productId = buffer.getProductId();
        String locationId = buffer.getLocationId();
        Integer bufferUnits = buffer.getBufferUnits() != null ? buffer.getBufferUnits() : 0;

        try {
            // Get yesterday's date range
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDateTime startOfYesterday = yesterday.atStartOfDay();
            LocalDateTime endOfYesterday = yesterday.atTime(23, 59, 59);

            log.debug("Calculating yesterday's consumption for product {} at location {} between {} and {}",
                    productId, locationId, startOfYesterday, endOfYesterday);

            // Get yesterday's consumption using the fixed repository method
            Optional<Integer> consumptionOpt = dailyConsumptionLogRepository
                    .sumQuantityConsumedBetweenDates(productId, locationId, startOfYesterday, endOfYesterday);

            Integer yesterdayConsumption = consumptionOpt.orElse(0);

            log.debug("Yesterday's consumption for product {} at location {}: {}",
                    productId, locationId, yesterdayConsumption);

            // Calculate current inventory = buffer_units - yesterday's consumption
            Integer currentInventory = bufferUnits - yesterdayConsumption;

            // Ensure current inventory is not negative
            currentInventory = Math.max(0, currentInventory);

            log.debug("Current inventory calculation for buffer {}: buffer_units({}) - yesterday_consumption({}) = {}",
                    buffer.getBufferId(), bufferUnits, yesterdayConsumption, currentInventory);

            return currentInventory;

        } catch (Exception e) {
            log.error("Error calculating current inventory for buffer {}: {}", buffer.getBufferId(), e.getMessage(), e);
            // Return buffer units as fallback if consumption calculation fails
            return bufferUnits;
        }
    }

    /**
     * Calculate buffer consumed percentage based on net available quantity
     */
    private Double calculateBufferConsumedPercentage(InventoryBuffer buffer, Integer netAvailableQty) {
        Integer bufferUnits = buffer.getBufferUnits() != null ? buffer.getBufferUnits() : 0;
        String bufferId = buffer.getBufferId();

        if (bufferUnits <= 0) {
            log.warn("Buffer {} has zero or negative buffer_units: {}", bufferId, bufferUnits);
            return 0.0;
        }

        Double consumedPct;

        if (netAvailableQty >= bufferUnits) {
            // If net available quantity exceeds buffer units, nothing is consumed (0% consumed)
            consumedPct = 0.0;
        } else if (netAvailableQty <= 0) {
            // If no inventory available, buffer is fully consumed (100% consumed)
            consumedPct = 100.0;
        } else {
            // Calculate percentage consumed: (buffer_units - net_available_qty) / buffer_units * 100
            consumedPct = ((double)(bufferUnits - netAvailableQty) / bufferUnits) * 100.0;
        }

        // Ensure percentage is between 0 and 100
        consumedPct = Math.max(0.0, Math.min(100.0, consumedPct));

        log.debug("Buffer consumed calculation for buffer {}: buffer_units={}, net_available_qty={}, consumed_pct={}%",
                bufferId, bufferUnits, netAvailableQty, String.format("%.2f", consumedPct));

        return consumedPct;
    }

    /**
     * Determine buffer zone based on consumed percentage and thresholds
     */
    private String determineBufferZone(InventoryBuffer buffer, Double bufferConsumedPct) {
        Double redThreshold = buffer.getRedThresholdPct() != null ? buffer.getRedThresholdPct() : 80.0;
        Double yellowThreshold = buffer.getYellowThresholdPct() != null ? buffer.getYellowThresholdPct() : 50.0;

        log.debug("Determining zone for buffer {}: consumed_pct={}%, red_threshold={}%, yellow_threshold={}%",
                buffer.getBufferId(), String.format("%.2f", bufferConsumedPct), redThreshold, yellowThreshold);

        // Higher consumed percentage means more critical
        if (bufferConsumedPct >= redThreshold) {
            return "RED";       // Critical: Buffer is highly consumed
        } else if (bufferConsumedPct >= yellowThreshold) {
            return "YELLOW";    // Warning: Buffer is moderately consumed
        } else {
            return "GREEN";     // Safe: Buffer has plenty available
        }
    }

    /**
     * Update consecutive zone days tracking
     */
    private void updateConsecutiveZoneDays(InventoryBuffer buffer, String originalZone,
                                           String newZone, Integer originalConsecutiveDays) {
        if (originalZone == null) {
            // First time zone assignment
            buffer.setConsecutiveZoneDays(1);
            log.debug("First zone assignment for buffer {}: {} (consecutive days = 1)",
                    buffer.getBufferId(), newZone);
            return;
        }

        if (Objects.equals(originalZone, newZone)) {
            // Same zone, increment consecutive days
            int newConsecutiveDays = (originalConsecutiveDays != null ? originalConsecutiveDays : 0) + 1;
            buffer.setConsecutiveZoneDays(newConsecutiveDays);
            log.debug("Zone unchanged for buffer {}: {} (consecutive days = {})",
                    buffer.getBufferId(), newZone, newConsecutiveDays);
        } else {
            // Zone changed, reset consecutive days to 1
            buffer.setConsecutiveZoneDays(1);
            log.info("Zone changed for buffer {}: {} -> {} (consecutive days reset to 1)",
                    buffer.getBufferId(), originalZone, newZone);
        }
    }

    /**
     * Get all unique location IDs that have active buffers
     */
    private Set<String> getAllActiveLocationIds() {
        try {
            Page<InventoryBuffer> activeBuffers = inventoryBufferRepository.findActiveBuffers(PageRequest.of(0, 10000));
            Set<String> locationIds = activeBuffers.getContent().stream()
                    .map(InventoryBuffer::getLocationId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            log.debug("Active location IDs: {}", locationIds);
            return locationIds;
        } catch (Exception e) {
            log.error("Error getting active location IDs: {}", e.getMessage(), e);
            return new HashSet<>();
        }
    }

    /**
     * Result class to track update statistics
     */
    public static class BufferUpdateResult {
        private int processedCount = 0;
        private int updatedCount = 0;
        private int errorCount = 0;

        public void incrementProcessed() { this.processedCount++; }
        public void incrementUpdated() { this.updatedCount++; }
        public void incrementError() { this.errorCount++; }

        public int getProcessedCount() { return processedCount; }
        public int getUpdatedCount() { return updatedCount; }
        public int getErrorCount() { return errorCount; }
    }
}

