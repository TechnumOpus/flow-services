package com.onified.distribute.service.impl;

import com.onified.distribute.dto.BufferAdjustmentLogDTO;
import com.onified.distribute.dto.ConsumptionProfileDTO;
import com.onified.distribute.dto.InventoryBufferDTO;
import com.onified.distribute.dto.LeadTimeDTO;
import com.onified.distribute.entity.InventoryBuffer;
import com.onified.distribute.repository.InventoryBufferRepository;
import com.onified.distribute.service.BufferAdjustmentLogService;
import com.onified.distribute.service.ConsumptionProfileService;
import com.onified.distribute.service.InventoryBufferService;
import com.onified.distribute.service.LeadTimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryBufferServiceImpl implements InventoryBufferService {

    private final InventoryBufferRepository inventoryBufferRepository;
    private final ConsumptionProfileService consumptionProfileService;
    private final LeadTimeService leadTimeService;
    private final BufferAdjustmentLogService bufferAdjustmentLogService;


    @Override
    public InventoryBufferDTO calculateAndCreateBuffer(String productId, String locationId, Double safetyFactor) {
        log.info("Calculating and creating buffer for product: {} at location: {} with safety factor: {}",
                productId, locationId, safetyFactor);

        // Step 1: Get buffer lead time days from lead_times collection
        Double bufferLeadTimeDays = leadTimeService.calculateTotalLeadTime(productId, locationId);
        if (bufferLeadTimeDays == null || bufferLeadTimeDays == 0.0) {
            throw new IllegalArgumentException("No valid lead time found for product: " + productId + " at location: " + locationId);
        }

        // Step 2: Get consumption data from consumption_profile collection
        ConsumptionProfileDTO consumptionProfile = consumptionProfileService.getConsumptionProfileByProductAndLocation(productId, locationId);

        // Use ADC30D as primary consumption metric (fallback to other ADCs if needed)
        Double adc = getOptimalADC(consumptionProfile);
        if (adc == null || adc <= 0) {
            throw new IllegalArgumentException("No valid consumption data found for product: " + productId + " at location: " + locationId);
        }

        // Step 3: Calculate buffer units
        // Formula: buffer_units = adc * buffer_lead_time_days * (1 + safety_factor)
        Double calculatedBufferUnits = adc * bufferLeadTimeDays * (1 + (safetyFactor != null ? safetyFactor : 0.2));
        Integer bufferUnits = (int) Math.ceil(calculatedBufferUnits);

        // Step 4: Calculate buffer days
        // Formula: buffer_days = buffer_units / adc
        Integer bufferDays = (int) Math.ceil(bufferUnits / adc);

        // Step 5: Calculate review period (e.g., 4 * buffer_lead_time_days for adjustment threshold)
        Integer reviewPeriodDays = (int) Math.ceil(4 * bufferLeadTimeDays);

        // Step 6: Create InventoryBufferDTO
        InventoryBufferDTO bufferDto = createBufferDTO(productId, locationId, bufferUnits, bufferDays,
                bufferLeadTimeDays.intValue(), reviewPeriodDays, adc);

        // Step 7: Create buffer using existing POST method
        InventoryBufferDTO createdBuffer = createInventoryBuffer(bufferDto);

        // Step 8: Automatically log buffer creation in BufferAdjustmentLog
        try {
            logBufferCreation(createdBuffer, adc, safetyFactor);
            log.info("Buffer creation logged successfully for buffer: {}", createdBuffer.getBufferId());
        } catch (Exception e) {
            log.error("Failed to log buffer creation for buffer: {}, error: {}", createdBuffer.getBufferId(), e.getMessage());
            // Don't fail the buffer creation if logging fails
        }

        log.info("Buffer created successfully - ID: {}, Units: {}, Days: {}",
                createdBuffer.getBufferId(), createdBuffer.getBufferUnits(), createdBuffer.getBufferDays());

        return createdBuffer;
    }

    // Add this private method to your InventoryBufferServiceImpl class

    private void logBufferCreation(InventoryBufferDTO createdBuffer, Double adcUsed, Double safetyFactor) {
        log.info("Logging buffer creation for buffer: {}", createdBuffer.getBufferId());

        try {
            BufferAdjustmentLogDTO adjustmentLogDto = new BufferAdjustmentLogDTO();

            // Basic identifiers
            adjustmentLogDto.setBufferId(createdBuffer.getBufferId());
            adjustmentLogDto.setProductId(createdBuffer.getProductId());
            adjustmentLogDto.setLocationId(createdBuffer.getLocationId());

            // Adjustment details
            adjustmentLogDto.setAdjustmentType("INITIAL_CREATION");
            adjustmentLogDto.setOldBufferDays(0); // No previous buffer
            adjustmentLogDto.setNewBufferDays(createdBuffer.getBufferDays());
            adjustmentLogDto.setOldBufferUnits(0); // No previous buffer
            adjustmentLogDto.setNewBufferUnits(createdBuffer.getBufferUnits());

            // Calculate change percentage (100% since it's new creation)
            adjustmentLogDto.setChangePercentage(100.0);

            // Trigger information
            adjustmentLogDto.setTriggerReason(String.format("Initial buffer creation with safety factor: %.2f, ADC: %.2f",
                    safetyFactor != null ? safetyFactor : 0.2, adcUsed));
            adjustmentLogDto.setConsecutiveDaysInZone(0);
            adjustmentLogDto.setZoneWhenTriggered("NEW");
            adjustmentLogDto.setAdcAtAdjustment(adcUsed);

            // System flags
            adjustmentLogDto.setSystemRecommended(true);
            adjustmentLogDto.setRequiresApproval(false); // Initial creation doesn't require approval
            adjustmentLogDto.setApprovalStatus("AUTO_APPROVED");
            adjustmentLogDto.setApprovedBy("SYSTEM");
            adjustmentLogDto.setApprovalDate(LocalDateTime.now());

            // Metadata
            adjustmentLogDto.setCreatedBy("SYSTEM");
            adjustmentLogDto.setComments("Automatic buffer creation based on lead time and consumption profile analysis");

            // Create the adjustment log
            BufferAdjustmentLogDTO createdLog = bufferAdjustmentLogService.createBufferAdjustmentLog(adjustmentLogDto);
            log.info("Buffer adjustment log created with ID: {} for buffer: {}",
                    createdLog.getLogId(), createdBuffer.getBufferId());

        } catch (Exception e) {
            log.error("Error creating buffer adjustment log for buffer: {}, error: {}",
                    createdBuffer.getBufferId(), e.getMessage(), e);
            throw new RuntimeException("Failed to log buffer creation", e);
        }
    }


    private Double getOptimalADC(ConsumptionProfileDTO profile) {
        // Priority: ADC30D > ADC14D > ADC7D > ADC60D
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

        // Basic identifiers
        bufferDto.setProductId(productId);
        bufferDto.setLocationId(locationId);

        // Buffer calculations
        bufferDto.setBufferUnits(bufferUnits);
        bufferDto.setBufferDays(bufferDays);
        bufferDto.setBufferLeadTimeDays(bufferLeadTimeDays);

        // Zone thresholds (standard percentages)
        bufferDto.setGreenThresholdPct(33.0);
        bufferDto.setYellowThresholdPct(33.0);
        bufferDto.setRedThresholdPct(33.0);

        // Review settings
        bufferDto.setDbmReviewPeriodDays(reviewPeriodDays);
        bufferDto.setAdjustmentThresholdDays(bufferLeadTimeDays * 3); // 3 RLT cycles

        // Initial inventory (set to 0, will be updated separately)
        bufferDto.setCurrentInventory(0);
        bufferDto.setInPipelineQty(0);

        // Status
        bufferDto.setIsActive(true);
        bufferDto.setCurrentZone("GREEN"); // Default zone

        // Timestamps
        LocalDateTime now = LocalDateTime.now();
        bufferDto.setCreatedAt(now);
        bufferDto.setUpdatedAt(now);
        bufferDto.setNextReviewDue(now.plusDays(reviewPeriodDays));

        // Metadata
        bufferDto.setCreatedBy("SYSTEM");
        bufferDto.setUpdatedBy("SYSTEM");

        return bufferDto;
    }

    @Override
    public InventoryBufferDTO createInventoryBuffer(InventoryBufferDTO bufferDto) {
        log.info("Creating inventory buffer for product: {} at location: {}",
                bufferDto.getProductId(), bufferDto.getLocationId());

        if (existsByProductAndLocation(bufferDto.getProductId(), bufferDto.getLocationId())) {
            throw new IllegalArgumentException("Buffer already exists for product and location combination");
        }

        InventoryBuffer buffer = convertToEntity(bufferDto);
        buffer.setBufferId(UUID.randomUUID().toString());
        buffer.setCreatedAt(LocalDateTime.now());
        buffer.setUpdatedAt(LocalDateTime.now());
        buffer.setIsActive(true);

        // Calculate initial zone
        buffer = calculateZone(buffer);

        InventoryBuffer savedBuffer = inventoryBufferRepository.save(buffer);
        return convertToDto(savedBuffer);
    }

    @Override
    public InventoryBufferDTO updateInventoryBuffer(String bufferId, InventoryBufferDTO bufferDto) {
        log.info("Updating inventory buffer: {}", bufferId);

        InventoryBuffer existingBuffer = inventoryBufferRepository.findById(bufferId)
                .orElseThrow(() -> new IllegalArgumentException("Buffer not found with ID: " + bufferId));

        updateEntityFromDto(existingBuffer, bufferDto);
        existingBuffer.setUpdatedAt(LocalDateTime.now());

        // Recalculate zone after update
        existingBuffer = calculateZone(existingBuffer);

        InventoryBuffer savedBuffer = inventoryBufferRepository.save(existingBuffer);
        return convertToDto(savedBuffer);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryBufferDTO getInventoryBufferById(String bufferId) {
        log.info("Fetching inventory buffer: {}", bufferId);

        InventoryBuffer buffer = inventoryBufferRepository.findById(bufferId)
                .orElseThrow(() -> new IllegalArgumentException("Buffer not found with ID: " + bufferId));

        return convertToDto(buffer);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryBufferDTO getInventoryBufferByProductAndLocation(String productId, String locationId) {
        log.info("Fetching inventory buffer for product: {} at location: {}", productId, locationId);

        InventoryBuffer buffer = inventoryBufferRepository.findByProductIdAndLocationId(productId, locationId)
                .orElseThrow(() -> new IllegalArgumentException("Buffer not found for product and location"));

        return convertToDto(buffer);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryBufferDTO> getAllInventoryBuffers(Pageable pageable) {
        log.info("Fetching all inventory buffers");
        return inventoryBufferRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryBufferDTO> getInventoryBuffersByProduct(String productId, Pageable pageable) {
        log.info("Fetching inventory buffers by product: {}", productId);
        return inventoryBufferRepository.findByProductId(productId, pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryBufferDTO> getInventoryBuffersByLocation(String locationId, Pageable pageable) {
        log.info("Fetching inventory buffers by location: {}", locationId);
        return inventoryBufferRepository.findByLocationId(locationId, pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryBufferDTO> getInventoryBuffersByCurrentZone(String currentZone, Pageable pageable) {
        log.info("Fetching inventory buffers by current zone: {}", currentZone);
        return inventoryBufferRepository.findByCurrentZone(currentZone, pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryBufferDTO> getActiveInventoryBuffers(Pageable pageable) {
        log.info("Fetching active inventory buffers");
        return inventoryBufferRepository.findActiveBuffers(pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryBufferDTO> getBuffersDueForReview(LocalDateTime currentDate, Pageable pageable) {
        log.info("Fetching buffers due for review as of: {}", currentDate);
        return inventoryBufferRepository.findBuffersDueForReview(currentDate, pageable).map(this::convertToDto);
    }

    @Override
    public InventoryBufferDTO initializeBuffer(String productId, String locationId, Double safetyFactor) {
        log.info("Initializing buffer for product: {} at location: {} with safety factor: {}",
                productId, locationId, safetyFactor);

        // Get lead time data
        LeadTimeDTO leadTime = leadTimeService.getLeadTimeByProductAndLocation(productId, locationId);

        // Get consumption profile data
        ConsumptionProfileDTO consumptionProfile = consumptionProfileService.getConsumptionProfileByProductAndLocation(productId, locationId);

        // Calculate buffer parameters
        Double bufferLeadTimeDays = leadTime.getBufferLeadTimeDays();
        Double adc30d = consumptionProfile.getAdc30d();

        if (bufferLeadTimeDays == null || adc30d == null) {
            throw new IllegalArgumentException("Missing required data for buffer calculation");
        }

        Integer bufferUnits = (int) Math.ceil(adc30d * bufferLeadTimeDays * (1 + safetyFactor));
        Integer bufferDays = (int) Math.ceil(bufferUnits / adc30d);

        // Create buffer DTO
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

        return createInventoryBuffer(bufferDto);
    }

    @Override
    public InventoryBufferDTO calculateBufferZone(String bufferId) {
        log.info("Calculating buffer zone for buffer: {}", bufferId);

        InventoryBuffer buffer = inventoryBufferRepository.findById(bufferId)
                .orElseThrow(() -> new IllegalArgumentException("Buffer not found with ID: " + bufferId));

        buffer = calculateZone(buffer);
        InventoryBuffer savedBuffer = inventoryBufferRepository.save(buffer);
        return convertToDto(savedBuffer);
    }

    @Override
    public InventoryBufferDTO updateBufferInventory(String bufferId, Integer currentInventory, Integer inPipelineQty) {
        log.info("Updating buffer inventory for buffer: {} with current: {}, pipeline: {}",
                bufferId, currentInventory, inPipelineQty);

        InventoryBuffer buffer = inventoryBufferRepository.findById(bufferId)
                .orElseThrow(() -> new IllegalArgumentException("Buffer not found with ID: " + bufferId));

        buffer.setCurrentInventory(currentInventory);
        buffer.setInPipelineQty(inPipelineQty);
        buffer.setNetAvailableQty(currentInventory + inPipelineQty);
        buffer.setUpdatedAt(LocalDateTime.now());

        // Recalculate zone
        buffer = calculateZone(buffer);

        InventoryBuffer savedBuffer = inventoryBufferRepository.save(buffer);
        return convertToDto(savedBuffer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryBufferDTO> getBuffersInZones(List<String> zones) {
        log.info("Fetching buffers in zones: {}", zones);
        return inventoryBufferRepository.findByCurrentZoneIn(zones, Pageable.unpaged())
                .getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteInventoryBuffer(String bufferId) {
        log.info("Deleting inventory buffer: {}", bufferId);

        if (!inventoryBufferRepository.existsById(bufferId)) {
            throw new IllegalArgumentException("Buffer not found with ID: " + bufferId);
        }

        inventoryBufferRepository.deleteById(bufferId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByProductAndLocation(String productId, String locationId) {
        return inventoryBufferRepository.existsByProductIdAndLocationId(productId, locationId);
    }

    private InventoryBuffer calculateZone(InventoryBuffer buffer) {
        if (buffer.getBufferUnits() == null || buffer.getBufferUnits() == 0) {
            buffer.setCurrentZone("UNKNOWN");
            buffer.setBufferConsumedPct(0.0);
            return buffer;
        }

        Integer netAvailable = buffer.getNetAvailableQty() != null ? buffer.getNetAvailableQty() :
                (buffer.getCurrentInventory() != null ? buffer.getCurrentInventory() : 0) +
                        (buffer.getInPipelineQty() != null ? buffer.getInPipelineQty() : 0);

        buffer.setNetAvailableQty(netAvailable);

        double consumedPct = ((double) (buffer.getBufferUnits() - netAvailable) / buffer.getBufferUnits()) * 100;
        buffer.setBufferConsumedPct(Math.max(0, Math.min(100, consumedPct)));

        // Determine zone based on thresholds
        if (consumedPct <= (100 - buffer.getGreenThresholdPct())) {
            buffer.setCurrentZone("GREEN");
        } else if (consumedPct <= (100 - buffer.getYellowThresholdPct())) {
            buffer.setCurrentZone("YELLOW");
        } else if (consumedPct <= (100 - buffer.getRedThresholdPct())) {
            buffer.setCurrentZone("RED");
        } else {
            buffer.setCurrentZone("CRITICAL");
        }

        return buffer;
    }

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

    private void updateEntityFromDto(InventoryBuffer existingBuffer, InventoryBufferDTO dto) {
        if (dto == null || existingBuffer == null) {
            return;
        }

        // Don't update id, bufferId, createdAt, createdBy as mentioned in original code
        if (dto.getProductId() != null) {
            existingBuffer.setProductId(dto.getProductId());
        }
        if (dto.getLocationId() != null) {
            existingBuffer.setLocationId(dto.getLocationId());
        }
        if (dto.getBufferType() != null) {
            existingBuffer.setBufferType(dto.getBufferType());
        }
        if (dto.getBufferDays() != null) {
            existingBuffer.setBufferDays(dto.getBufferDays());
        }
        if (dto.getBufferUnits() != null) {
            existingBuffer.setBufferUnits(dto.getBufferUnits());
        }
        if (dto.getGreenThresholdPct() != null) {
            existingBuffer.setGreenThresholdPct(dto.getGreenThresholdPct());
        }
        if (dto.getYellowThresholdPct() != null) {
            existingBuffer.setYellowThresholdPct(dto.getYellowThresholdPct());
        }
        if (dto.getRedThresholdPct() != null) {
            existingBuffer.setRedThresholdPct(dto.getRedThresholdPct());
        }
        if (dto.getCurrentInventory() != null) {
            existingBuffer.setCurrentInventory(dto.getCurrentInventory());
        }
        if (dto.getInPipelineQty() != null) {
            existingBuffer.setInPipelineQty(dto.getInPipelineQty());
        }
        if (dto.getNetAvailableQty() != null) {
            existingBuffer.setNetAvailableQty(dto.getNetAvailableQty());
        }
        if (dto.getBufferConsumedPct() != null) {
            existingBuffer.setBufferConsumedPct(dto.getBufferConsumedPct());
        }
        if (dto.getCurrentZone() != null) {
            existingBuffer.setCurrentZone(dto.getCurrentZone());
        }
        if (dto.getReviewCycleId() != null) {
            existingBuffer.setReviewCycleId(dto.getReviewCycleId());
        }
        if (dto.getDbmReviewPeriodDays() != null) {
            existingBuffer.setDbmReviewPeriodDays(dto.getDbmReviewPeriodDays());
        }
        if (dto.getLastReviewDate() != null) {
            existingBuffer.setLastReviewDate(dto.getLastReviewDate());
        }
        if (dto.getNextReviewDue() != null) {
            existingBuffer.setNextReviewDue(dto.getNextReviewDue());
        }
        if (dto.getConsecutiveZoneDays() != null) {
            existingBuffer.setConsecutiveZoneDays(dto.getConsecutiveZoneDays());
        }
        if (dto.getAdjustmentThresholdDays() != null) {
            existingBuffer.setAdjustmentThresholdDays(dto.getAdjustmentThresholdDays());
        }
        if (dto.getIsActive() != null) {
            existingBuffer.setIsActive(dto.getIsActive());
        }
        if (dto.getUpdatedBy() != null) {
            existingBuffer.setUpdatedBy(dto.getUpdatedBy());
        }
    }
}
