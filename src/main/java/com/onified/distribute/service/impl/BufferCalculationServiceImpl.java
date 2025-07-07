package com.onified.distribute.service.impl;

import com.onified.distribute.dto.BufferAdjustmentLogDTO;
import com.onified.distribute.dto.ConsumptionProfileDTO;
import com.onified.distribute.dto.InventoryBufferDTO;
import com.onified.distribute.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BufferCalculationServiceImpl implements BufferCalculationService {

    private final InventoryBufferService inventoryBufferService;
    private final ConsumptionProfileService consumptionProfileService;
    private final LeadTimeService leadTimeService;
    private final BufferAdjustmentLogService bufferAdjustmentLogService;

    @Override
    public InventoryBufferDTO calculateAndCreateBuffer(String productId, String locationId, Double safetyFactor) {
        log.info("Calculating and creating buffer for product: {} at location: {} with safety factor: {}",
                productId, locationId, safetyFactor);

        Double bufferLeadTimeDays = leadTimeService.calculateTotalLeadTime(productId, locationId);
        if (bufferLeadTimeDays == null || bufferLeadTimeDays == 0.0) {
            throw new IllegalArgumentException("No valid lead time found for product: " + productId + " at location: " + locationId);
        }

        ConsumptionProfileDTO consumptionProfile = consumptionProfileService.getConsumptionProfileByProductAndLocation(productId, locationId);
        Double adc = getOptimalADC(consumptionProfile);
        if (adc == null || adc <= 0) {
            throw new IllegalArgumentException("No valid consumption data found for product: " + productId + " at location: " + locationId);
        }

        Double calculatedBufferUnits = adc * bufferLeadTimeDays * (1 + (safetyFactor != null ? safetyFactor : 0.2));
        Integer bufferUnits = (int) Math.ceil(calculatedBufferUnits);
        Integer bufferDays = (int) Math.ceil(bufferUnits / adc);
        Integer reviewPeriodDays = (int) Math.ceil(4 * bufferLeadTimeDays);

        InventoryBufferDTO bufferDto = createBufferDTO(productId, locationId, bufferUnits, bufferDays,
                bufferLeadTimeDays.intValue(), reviewPeriodDays, adc);
        InventoryBufferDTO createdBuffer = inventoryBufferService.createInventoryBuffer(bufferDto);

        try {
            logBufferCreation(createdBuffer, adc, safetyFactor);
            log.info("Buffer creation logged successfully for buffer: {}", createdBuffer.getBufferId());
        } catch (Exception e) {
            log.error("Failed to log buffer creation for buffer: {}, error: {}", createdBuffer.getBufferId(), e.getMessage());
        }

        log.info("Buffer created successfully - ID: {}, Units: {}, Days: {}",
                createdBuffer.getBufferId(), createdBuffer.getBufferUnits(), createdBuffer.getBufferDays());
        return createdBuffer;
    }

    @Override
    public InventoryBufferDTO initializeBuffer(String productId, String locationId, Double safetyFactor) {
        log.info("Initializing buffer for product: {} at location: {} with safety factor: {}",
                productId, locationId, safetyFactor);

        Double bufferLeadTimeDays = leadTimeService.calculateTotalLeadTime(productId, locationId);
        ConsumptionProfileDTO consumptionProfile = consumptionProfileService.getConsumptionProfileByProductAndLocation(productId, locationId);

        if (bufferLeadTimeDays == null || consumptionProfile.getAdc30d() == null) {
            throw new IllegalArgumentException("Missing required data for buffer calculation");
        }

        Integer bufferUnits = (int) Math.ceil(consumptionProfile.getAdc30d() * bufferLeadTimeDays * (1 + safetyFactor));
        Integer bufferDays = (int) Math.ceil(bufferUnits / consumptionProfile.getAdc30d());

        InventoryBufferDTO bufferDto = new InventoryBufferDTO();
        bufferDto.setProductId(productId);
        bufferDto.setLocationId(locationId);
        bufferDto.setBufferType("DYNAMIC");
        bufferDto.setBufferDays(bufferDays);
        bufferDto.setBufferUnits(bufferUnits);
        bufferDto.setGreenThresholdPct(80.0);
        bufferDto.setYellowThresholdPct(50.0);
        bufferDto.setRedThresholdPct(20.0);
        bufferDto.setCurrentInventory(0);
        bufferDto.setInPipelineQty(0);
        bufferDto.setDbmReviewPeriodDays(7);
        bufferDto.setConsecutiveZoneDays(0);
        bufferDto.setAdjustmentThresholdDays(bufferDays * 3);
        bufferDto.setNextReviewDue(LocalDateTime.now().plusDays(7));
        bufferDto.setIsActive(true);

        return inventoryBufferService.createInventoryBuffer(bufferDto);
    }

    private void logBufferCreation(InventoryBufferDTO createdBuffer, Double adcUsed, Double safetyFactor) {
        BufferAdjustmentLogDTO adjustmentLogDto = new BufferAdjustmentLogDTO();
        adjustmentLogDto.setBufferId(createdBuffer.getBufferId());
        adjustmentLogDto.setProductId(createdBuffer.getProductId());
        adjustmentLogDto.setLocationId(createdBuffer.getLocationId());
        adjustmentLogDto.setAdjustmentType("INITIAL_CREATION");
        adjustmentLogDto.setOldBufferDays(0);
        adjustmentLogDto.setNewBufferDays(createdBuffer.getBufferDays());
        adjustmentLogDto.setOldBufferUnits(0);
        adjustmentLogDto.setNewBufferUnits(createdBuffer.getBufferUnits());
        adjustmentLogDto.setChangePercentage(100.0);
        adjustmentLogDto.setTriggerReason(String.format("Initial buffer creation with safety factor: %.2f, ADC: %.2f",
                safetyFactor != null ? safetyFactor : 0.2, adcUsed));
        adjustmentLogDto.setConsecutiveDaysInZone(0);
        adjustmentLogDto.setZoneWhenTriggered("NEW");
        adjustmentLogDto.setAdcAtAdjustment(adcUsed);
        adjustmentLogDto.setSystemRecommended(true);
        adjustmentLogDto.setRequiresApproval(false);
        adjustmentLogDto.setApprovalStatus("AUTO_APPROVED");
        adjustmentLogDto.setApprovedBy("SYSTEM");
        adjustmentLogDto.setApprovalDate(LocalDateTime.now());
        adjustmentLogDto.setCreatedBy("SYSTEM");
        adjustmentLogDto.setComments("Automatic buffer creation based on lead time and consumption profile analysis");

        bufferAdjustmentLogService.createBufferAdjustmentLog(adjustmentLogDto);
    }

    private Double getOptimalADC(ConsumptionProfileDTO profile) {
        if (profile.getAdc30d() != null && profile.getAdc30d() > 0) {
            return profile.getAdc30d();
        } else if (profile.getAdc14d() != null && profile.getAdc14d() > 0) {
            return profile.getAdc14d();
        } else if (profile.getAdc7d() != null && profile.getAdc7d() > 0) {
            return profile.getAdc7d();
        } else if (profile.getAdc60d() != null && profile.getAdc60d() > 0) {
            return profile.getAdc60d();
        }
        return null;
    }

    private InventoryBufferDTO createBufferDTO(String productId, String locationId, Integer bufferUnits,
                                               Integer bufferDays, Integer bufferLeadTimeDays,
                                               Integer reviewPeriodDays, Double adc) {
        InventoryBufferDTO bufferDto = new InventoryBufferDTO();
        bufferDto.setProductId(productId);
        bufferDto.setLocationId(locationId);
        bufferDto.setBufferUnits(bufferUnits);
        bufferDto.setBufferDays(bufferDays);
        bufferDto.setBufferLeadTimeDays(bufferLeadTimeDays);
        bufferDto.setGreenThresholdPct(33.0);
        bufferDto.setYellowThresholdPct(33.0);
        bufferDto.setRedThresholdPct(33.0);
        bufferDto.setDbmReviewPeriodDays(reviewPeriodDays);
        bufferDto.setAdjustmentThresholdDays(bufferLeadTimeDays * 3);
        bufferDto.setCurrentInventory(0);
        bufferDto.setInPipelineQty(0);
        bufferDto.setIsActive(true);
        bufferDto.setCurrentZone("GREEN");
        LocalDateTime now = LocalDateTime.now();
        bufferDto.setCreatedAt(now);
        bufferDto.setUpdatedAt(now);
        bufferDto.setNextReviewDue(now.plusDays(reviewPeriodDays));
        bufferDto.setCreatedBy("SYSTEM");
        bufferDto.setUpdatedBy("SYSTEM");
        return bufferDto;
    }
}
