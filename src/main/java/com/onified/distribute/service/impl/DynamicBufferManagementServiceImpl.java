package com.onified.distribute.service.impl;

import com.onified.distribute.dto.BufferAdjustmentLogDTO;
import com.onified.distribute.dto.ConsumptionProfileDTO;
import com.onified.distribute.dto.InventoryBufferDTO;
import com.onified.distribute.entity.InventoryBuffer;
import com.onified.distribute.repository.InventoryBufferRepository;
import com.onified.distribute.service.BufferAdjustmentLogService;
import com.onified.distribute.service.ConsumptionProfileService;
import com.onified.distribute.service.SeasonalityAdjustmentService;
import com.onified.distribute.service.SpecialEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DynamicBufferManagementServiceImpl {

    private final InventoryBufferRepository inventoryBufferRepository;
    private final ConsumptionProfileService consumptionProfileService;
    private final BufferAdjustmentLogService bufferAdjustmentLogService;
    private final SeasonalityAdjustmentService seasonalityAdjustmentService;
    private final SpecialEventService specialEventService;

    /**
     * Reviews and adjusts buffers due for review based on DBM logic.
     * @param currentDate The date to check for due reviews
     * @param pageable Pagination parameters
     * @return Map with summary of processed buffers
     */
    public Map<String, Object> reviewAndAdjustBuffers(LocalDateTime currentDate, Pageable pageable) {
        log.info("Starting DBM review process for buffers due as of: {}", currentDate);

        Map<String, Object> result = new HashMap<>();
        int processedCount = 0;
        int adjustedCount = 0;
        int errorCount = 0;

        try {
            Page<InventoryBufferDTO> buffersDue = inventoryBufferRepository
                    .findBuffersDueForReview(currentDate, pageable)
                    .map(this::convertToDto);

            for (InventoryBufferDTO buffer : buffersDue) {
                processedCount++;
                try {
                    boolean adjusted = adjustBufferIfNeeded(buffer, currentDate);
                    if (adjusted) {
                        adjustedCount++;
                    }
                } catch (Exception e) {
                    log.error("Error processing buffer {}: {}", buffer.getBufferId(), e.getMessage());
                    errorCount++;
                }
            }

            result.put("success", true);
            result.put("message", "DBM review completed");
            result.put("processedCount", processedCount);
            result.put("adjustedCount", adjustedCount);
            result.put("errorCount", errorCount);
            result.put("timestamp", System.currentTimeMillis());

        } catch (Exception e) {
            log.error("DBM review failed: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "DBM review failed: " + e.getMessage());
            result.put("processedCount", processedCount);
            result.put("adjustedCount", adjustedCount);
            result.put("errorCount", errorCount + 1);
            result.put("timestamp", System.currentTimeMillis());
        }

        return result;
    }

    /**
     * Adjusts a single buffer based on DBM rules.
     * @param buffer The buffer to adjust
     * @param currentDate The current date for review
     * @return true if the buffer was adjusted, false otherwise
     */
    private boolean adjustBufferIfNeeded(InventoryBufferDTO buffer, LocalDateTime currentDate) {
        if (buffer.getConsecutiveZoneDays() == null ||
                buffer.getAdjustmentThresholdDays() == null ||
                buffer.getConsecutiveZoneDays() < buffer.getAdjustmentThresholdDays()) {
            log.debug("Buffer {} not eligible for adjustment: consecutiveZoneDays={} < adjustmentThresholdDays={}",
                    buffer.getBufferId(), buffer.getConsecutiveZoneDays(), buffer.getAdjustmentThresholdDays());
            return false;
        }

        String zone = buffer.getCurrentZone();
        if (!"RED".equalsIgnoreCase(zone) && !"GREEN".equalsIgnoreCase(zone)) {
            log.debug("Buffer {} in {} zone, no adjustment needed", buffer.getBufferId(), zone);
            return false;
        }

        try {
            // Get consumption profile for ADC
            ConsumptionProfileDTO consumptionProfile = consumptionProfileService
                    .getConsumptionProfileByProductAndLocation(buffer.getProductId(), buffer.getLocationId());
            Double adcNormalized = consumptionProfile.getAdcNormalized();
            if (adcNormalized == null || adcNormalized <= 0) {
                throw new IllegalStateException("Invalid adc_normalized for buffer: " + buffer.getBufferId());
            }

            // Get seasonality and event factors
            Integer currentMonth = currentDate.getMonthValue();
            Double seasonalityFactor = seasonalityAdjustmentService.getSeasonalityFactor(
                    buffer.getProductId(), buffer.getLocationId(), currentMonth);
            Double eventImpactFactor = specialEventService.getEventImpactFactor(
                    buffer.getProductId(), buffer.getLocationId(), currentDate);

            // Calculate adjustment
            Integer oldBufferUnits = buffer.getBufferUnits();
            Integer newBufferUnits;
            double adjustmentFactor = 1.0;

            if ("RED".equalsIgnoreCase(zone)) {
                adjustmentFactor = 1.33 * seasonalityFactor * eventImpactFactor; // 33% increase
            } else { // GREEN
                adjustmentFactor = 0.67 * seasonalityFactor * eventImpactFactor; // 33% decrease
            }

            newBufferUnits = (int) Math.ceil(oldBufferUnits * adjustmentFactor);
            Integer newBufferDays = (int) Math.ceil(newBufferUnits / adcNormalized);

            // Log adjustment
            BufferAdjustmentLogDTO logDto = new BufferAdjustmentLogDTO();
            logDto.setBufferId(buffer.getBufferId());
            logDto.setProductId(buffer.getProductId());
            logDto.setLocationId(buffer.getLocationId());
            logDto.setAdjustmentType("RED".equalsIgnoreCase(zone) ? "INCREASE" : "DECREASE");
            logDto.setOldBufferUnits(oldBufferUnits);
            logDto.setNewBufferUnits(newBufferUnits);
            logDto.setOldBufferDays(buffer.getBufferDays());
            logDto.setNewBufferDays(newBufferDays);
            logDto.setChangePercentage(((double) (newBufferUnits - oldBufferUnits) / oldBufferUnits) * 100);
            logDto.setTriggerReason("consecutive_" + zone.toLowerCase());
            logDto.setConsecutiveDaysInZone(buffer.getConsecutiveZoneDays());
            logDto.setZoneWhenTriggered(zone);
            logDto.setAdcAtAdjustment(adcNormalized);
            logDto.setSystemRecommended(true);
            logDto.setRequiresApproval("RED".equalsIgnoreCase(zone));
            logDto.setApprovalStatus("RED".equalsIgnoreCase(zone) ? "PENDING" : "AUTO_APPROVED");
            logDto.setCreatedBy("SYSTEM");
            logDto.setAdjustmentDate(LocalDateTime.now());
            logDto.setComments(String.format("DBM adjustment: %s zone, seasonality factor: %.2f, event factor: %.2f",
                    zone, seasonalityFactor, eventImpactFactor));

            bufferAdjustmentLogService.createBufferAdjustmentLog(logDto);

            // Update buffer
            buffer.setBufferUnits(newBufferUnits);
            buffer.setBufferDays(newBufferDays);
            buffer.setConsecutiveZoneDays(0);
            buffer.setLastReviewDate(currentDate);
            buffer.setNextReviewDue(currentDate.plusDays(buffer.getDbmReviewPeriodDays()));
            buffer.setUpdatedAt(currentDate);
            buffer.setUpdatedBy("SYSTEM");

            // Save updated buffer
            InventoryBuffer entity = convertToEntity(buffer);
            inventoryBufferRepository.save(entity);

            log.info("Adjusted buffer {}: {} units -> {} units, {} days -> {} days, zone: {}",
                    buffer.getBufferId(), oldBufferUnits, newBufferUnits, buffer.getBufferDays(), newBufferDays, zone);
            return true;

        } catch (Exception e) {
            log.error("Failed to adjust buffer {}: {}", buffer.getBufferId(), e.getMessage());
            throw new RuntimeException("Buffer adjustment failed", e);
        }
    }

    // Existing convertToDto and convertToEntity methods (copied for completeness)
    private InventoryBufferDTO convertToDto(InventoryBuffer buffer) {
        if (buffer == null) {
            return null;
        }
        InventoryBufferDTO dto = new InventoryBufferDTO();
        dto.setId(buffer.getId());
        dto.setBufferId(buffer.getBufferId());
        dto.setProductId(buffer.getProductId());
        dto.setLocationId(buffer.getLocationId());
        dto.setBufferType(buffer.getBufferType());
        dto.setBufferDays(buffer.getBufferDays());
        dto.setBufferUnits(buffer.getBufferUnits());
        dto.setGreenThresholdPct(buffer.getGreenThresholdPct());
        dto.setYellowThresholdPct(buffer.getYellowThresholdPct());
        dto.setRedThresholdPct(buffer.getRedThresholdPct());
        dto.setCurrentInventory(buffer.getCurrentInventory());
        dto.setInPipelineQty(buffer.getInPipelineQty());
        dto.setNetAvailableQty(buffer.getNetAvailableQty());
        dto.setBufferConsumedPct(buffer.getBufferConsumedPct());
        dto.setCurrentZone(buffer.getCurrentZone());
        dto.setReviewCycleId(buffer.getReviewCycleId());
        dto.setDbmReviewPeriodDays(buffer.getDbmReviewPeriodDays());
        dto.setLastReviewDate(buffer.getLastReviewDate());
        dto.setNextReviewDue(buffer.getNextReviewDue());
        dto.setConsecutiveZoneDays(buffer.getConsecutiveZoneDays());
        dto.setAdjustmentThresholdDays(buffer.getAdjustmentThresholdDays());
        dto.setIsActive(buffer.getIsActive());
        dto.setCreatedAt(buffer.getCreatedAt());
        dto.setUpdatedAt(buffer.getUpdatedAt());
        dto.setCreatedBy(buffer.getCreatedBy());
        dto.setUpdatedBy(buffer.getUpdatedBy());
        return dto;
    }

    private InventoryBuffer convertToEntity(InventoryBufferDTO dto) {
        if (dto == null) {
            return null;
        }
        InventoryBuffer buffer = new InventoryBuffer();
        buffer.setId(dto.getId());
        buffer.setBufferId(dto.getBufferId());
        buffer.setProductId(dto.getProductId());
        buffer.setLocationId(dto.getLocationId());
        buffer.setBufferType(dto.getBufferType());
        buffer.setBufferDays(dto.getBufferDays());
        buffer.setBufferUnits(dto.getBufferUnits());
        buffer.setGreenThresholdPct(dto.getGreenThresholdPct());
        buffer.setYellowThresholdPct(dto.getYellowThresholdPct());
        buffer.setRedThresholdPct(dto.getRedThresholdPct());
        buffer.setCurrentInventory(dto.getCurrentInventory());
        buffer.setInPipelineQty(dto.getInPipelineQty());
        buffer.setNetAvailableQty(dto.getNetAvailableQty());
        buffer.setBufferConsumedPct(dto.getBufferConsumedPct());
        buffer.setCurrentZone(dto.getCurrentZone());
        buffer.setReviewCycleId(dto.getReviewCycleId());
        buffer.setDbmReviewPeriodDays(dto.getDbmReviewPeriodDays());
        buffer.setLastReviewDate(dto.getLastReviewDate());
        buffer.setNextReviewDue(dto.getNextReviewDue());
        buffer.setConsecutiveZoneDays(dto.getConsecutiveZoneDays());
        buffer.setAdjustmentThresholdDays(dto.getAdjustmentThresholdDays());
        buffer.setIsActive(dto.getIsActive());
        buffer.setCreatedAt(dto.getCreatedAt());
        buffer.setUpdatedAt(dto.getUpdatedAt());
        buffer.setCreatedBy(dto.getCreatedBy());
        buffer.setUpdatedBy(dto.getUpdatedBy());
        return buffer;
    }
}