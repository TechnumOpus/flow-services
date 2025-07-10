package com.onified.distribute.service.impl;

import com.onified.distribute.dto.*;
import com.onified.distribute.entity.InventoryBuffer;
import com.onified.distribute.repository.InventoryBufferRepository;
import com.onified.distribute.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BufferReviewServiceImpl implements BufferReviewService {

    private final InventoryBufferRepository inventoryBufferRepository;
    private final InventoryBufferService inventoryBufferService;
    private final BufferAdjustmentLogService bufferAdjustmentLogService;
    private final ConsumptionProfileService consumptionProfileService;
    private final LeadTimeService leadTimeService;
    private final ProductService productService;
    private final LocationService locationService;

    // DBM Configuration Constants
    private static final int DEFAULT_CONSECUTIVE_THRESHOLD_DAYS = 3;
    private static final int DEFAULT_LOCK_PERIOD_CYCLES = 6;
    private static final double DEFAULT_INCREASE_PERCENTAGE = 33.0;
    private static final double DEFAULT_DECREASE_PERCENTAGE = 33.0;
    private static final double APPROVAL_THRESHOLD_PERCENTAGE = 20.0;

    @Override
    @Transactional(readOnly = true)
    public Page<BufferReviewDTO> getBufferReviewData(String productId, String locationId,
                                                     String currentZone, Boolean dueForReview,
                                                     Pageable pageable) {
        log.info("Fetching buffer review data with filters - productId: {}, locationId: {}, currentZone: {}, dueForReview: {}",
                productId, locationId, currentZone, dueForReview);

        Page<InventoryBuffer> buffers;
        LocalDateTime now = LocalDateTime.now();

        if (productId != null && locationId != null) {
            Optional<InventoryBuffer> buffer = inventoryBufferRepository.findByProductIdAndLocationId(productId, locationId);
            List<InventoryBuffer> bufferList = buffer.map(List::of).orElse(List.of());
            buffers = new PageImpl<>(bufferList, pageable, bufferList.size());
        } else if (productId != null) {
            buffers = inventoryBufferRepository.findByProductId(productId, pageable);
        } else if (locationId != null) {
            buffers = inventoryBufferRepository.findByLocationId(locationId, pageable);
        } else if (currentZone != null) {
            buffers = inventoryBufferRepository.findByCurrentZone(currentZone, pageable);
        } else if (Boolean.TRUE.equals(dueForReview)) {
            buffers = inventoryBufferRepository.findBuffersDueForReview(now, pageable);
        } else {
            buffers = inventoryBufferRepository.findActiveBuffers(pageable);
        }

        List<BufferReviewDTO> reviewDTOs = buffers.getContent().stream()
                .map(this::convertToBufferReviewDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(reviewDTOs, pageable, buffers.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public BufferReviewDTO getBufferReviewById(String bufferId) {
        log.info("Fetching buffer review data for buffer: {}", bufferId);

        InventoryBuffer buffer = inventoryBufferRepository.findById(bufferId)
                .orElseThrow(() -> new IllegalArgumentException("Buffer not found with ID: " + bufferId));

        return convertToBufferReviewDTO(buffer);
    }

    @Override
    public BufferAdjustmentLogDTO adjustBuffer(BufferAdjustmentRequestDTO request) {
        log.info("Adjusting buffer: {} with recommended units: {}",
                request.getBufferId(), request.getRecommendedBufferUnits());

        InventoryBuffer buffer = inventoryBufferRepository.findById(request.getBufferId())
                .orElseThrow(() -> new IllegalArgumentException("Buffer not found with ID: " + request.getBufferId()));

        Integer oldBufferUnits = buffer.getBufferUnits();
        Integer newBufferUnits = request.getRecommendedBufferUnits();

        double changePercentage = (oldBufferUnits != null && oldBufferUnits > 0)
                ? ((double) (newBufferUnits - oldBufferUnits) / oldBufferUnits) * 100
                : 0.0;

        String adjustmentType = determineAdjustmentType(oldBufferUnits, newBufferUnits);

        buffer.setBufferUnits(newBufferUnits);
        buffer.setLastReviewDate(LocalDateTime.now());
        buffer.setNextReviewDue(calculateNextReviewDue(buffer));
        buffer.setUpdatedAt(LocalDateTime.now());
        buffer.setUpdatedBy(request.getUpdatedBy());
        buffer.setConsecutiveZoneDays(0); // Reset after adjustment

        inventoryBufferRepository.save(buffer);

        BufferAdjustmentLogDTO logDto = BufferAdjustmentLogDTO.builder()
                .bufferId(buffer.getBufferId())
                .productId(buffer.getProductId())
                .locationId(buffer.getLocationId())
                .adjustmentType(adjustmentType)
                .proposedBufferUnits(newBufferUnits)
                .safetyBufferUnits(calculateSafetyBuffer(buffer)) // Placeholder, refine with consumption data
                .finalBufferUnits(newBufferUnits)
                .changePercentage(changePercentage)
                .triggerReason(request.getAdjustmentReason() != null ? request.getAdjustmentReason() : "Manual adjustment")
                .consecutiveDaysInZone(buffer.getConsecutiveZoneDays())
                .systemRecommended(request.getOverrideSystemRecommendation() == null || !request.getOverrideSystemRecommendation())
                .requiresApproval(Math.abs(changePercentage) > APPROVAL_THRESHOLD_PERCENTAGE)
                .adjustmentDate(LocalDateTime.now())
                .createdBy(request.getUpdatedBy())
                .comments(request.getComments())
                .build();

        return bufferAdjustmentLogService.createBufferAdjustmentLog(logDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BufferReviewDTO> calculateDbmRecommendations() {
        log.info("Calculating DBM recommendations for all active buffers");

        List<InventoryBuffer> activeBuffers = inventoryBufferRepository.findActiveBuffers(Pageable.unpaged()).getContent();
        return activeBuffers.stream()
                .map(this::convertToBufferReviewDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BufferReviewDTO calculateDbmRecommendation(String bufferId) {
        log.info("Calculating DBM recommendation for buffer: {}", bufferId);

        InventoryBuffer buffer = inventoryBufferRepository.findById(bufferId)
                .orElseThrow(() -> new IllegalArgumentException("Buffer not found with ID: " + bufferId));
        return convertToBufferReviewDTO(buffer);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BufferReviewDTO> getBuffersDueForReview(Pageable pageable) {
        log.info("Fetching buffers due for review");

        LocalDateTime now = LocalDateTime.now();
        Page<InventoryBuffer> buffers = inventoryBufferRepository.findBuffersDueForReview(now, pageable);

        List<BufferReviewDTO> reviewDTOs = buffers.getContent().stream()
                .map(this::convertToBufferReviewDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(reviewDTOs, pageable, buffers.getTotalElements());
    }

    @Override
    public List<BufferAdjustmentLogDTO> bulkAdjustBuffers(List<BufferAdjustmentRequestDTO> requests) {
        log.info("Bulk adjusting {} buffers", requests.size());

        List<BufferAdjustmentLogDTO> adjustmentLogs = new ArrayList<>();
        for (BufferAdjustmentRequestDTO request : requests) {
            try {
                BufferAdjustmentLogDTO log = adjustBuffer(request);
                adjustmentLogs.add(log);
            } catch (Exception e) {
                log.error("Failed to adjust buffer: {}", request.getBufferId(), e);
            }
        }
        return adjustmentLogs;
    }

    private BufferReviewDTO convertToBufferReviewDTO(InventoryBuffer buffer) {
        ConsumptionProfileDTO consumptionProfile = null;
        try {
            consumptionProfile = consumptionProfileService.getConsumptionProfileByProductAndLocation(
                    buffer.getProductId(), buffer.getLocationId());
        } catch (Exception e) {
            log.warn("No consumption profile found for product: {} at location: {}",
                    buffer.getProductId(), buffer.getLocationId());
        }

        Double rlt = null;
        try {
            rlt = leadTimeService.calculateTotalLeadTime(buffer.getProductId(), buffer.getLocationId());
        } catch (Exception e) {
            log.warn("No lead time found for product: {} at location: {}",
                    buffer.getProductId(), buffer.getLocationId());
        }

        String productName = getProductName(buffer.getProductId());
        String locationName = getLocationName(buffer.getLocationId());
        String suggestedAction = calculateSuggestedAction(buffer, consumptionProfile, rlt);
        Integer recommendedBufferUnits = calculateRecommendedBufferUnits(buffer, consumptionProfile, rlt);
        Double changePercentage = (buffer.getBufferUnits() != null && buffer.getBufferUnits() > 0 && recommendedBufferUnits != null)
                ? ((double) (recommendedBufferUnits - buffer.getBufferUnits()) / buffer.getBufferUnits()) * 100
                : 0.0;
        String triggerReason = determineTriggerReason(buffer, suggestedAction);
        boolean requiresApproval = Math.abs(changePercentage) > APPROVAL_THRESHOLD_PERCENTAGE;

        return BufferReviewDTO.builder()
                .bufferId(buffer.getBufferId())
                .productId(buffer.getProductId())
                .locationId(buffer.getLocationId())
                .productName(productName)
                .locationName(locationName)
                .lastReviewDate(buffer.getLastReviewDate())
                .consecutiveZoneDays(buffer.getConsecutiveZoneDays())
                .currentBufferUnits(buffer.getBufferUnits())
                .recommendedBufferUnits(recommendedBufferUnits)
                .nextReviewDue(buffer.getNextReviewDue())
                .suggestedAction(suggestedAction)
                .currentZone(buffer.getCurrentZone())
                .bufferConsumedPct(buffer.getBufferConsumedPct())
                .adjustmentThresholdDays(buffer.getAdjustmentThresholdDays())
                .dbmReviewPeriodDays(buffer.getDbmReviewPeriodDays())
                .triggerReason(triggerReason)
                .changePercentage(changePercentage)
                .requiresApproval(requiresApproval)
                .baseADC(consumptionProfile != null ? consumptionProfile.getAdcNormalized() : null)
                .rlt(rlt)
                .netAvailableQty(buffer.getNetAvailableQty())
                .currentInventory(buffer.getCurrentInventory())
                .inPipelineQty(buffer.getInPipelineQty())
                .adcTrend(consumptionProfile != null ? consumptionProfile.getAdcTrend() : null)
                .trendConfidence(consumptionProfile != null ? consumptionProfile.getTrendConfidence() : null)
                .calculationDate(LocalDateTime.now())
                .build();
    }

    private String calculateSuggestedAction(InventoryBuffer buffer, ConsumptionProfileDTO consumptionProfile, Double rlt) {
        if (buffer.getConsecutiveZoneDays() == null || buffer.getAdjustmentThresholdDays() == null ||
                buffer.getConsecutiveZoneDays() < getAdjustmentThresholdDays(buffer) || isBufferLocked(buffer)) {
            return "MAINTAIN_BUFFER";
        }

        if ("RED".equals(buffer.getCurrentZone()) || "CRITICAL".equals(buffer.getCurrentZone())) {
            return "INCREASE_BUFFER";
        } else if ("GREEN".equals(buffer.getCurrentZone())) {
            return "DECREASE_BUFFER";
        }
        return "MAINTAIN_BUFFER";
    }

    private Integer calculateRecommendedBufferUnits(InventoryBuffer buffer, ConsumptionProfileDTO consumptionProfile, Double rlt) {
        if (consumptionProfile == null || rlt == null || rlt <= 0) {
            return buffer.getBufferUnits(); // Maintain current if data is insufficient
        }

        Double baseADC = consumptionProfile.getAdcNormalized();
        Integer currentUnits = buffer.getBufferUnits();
        String suggestedAction = calculateSuggestedAction(buffer, consumptionProfile, rlt);

        if ("INCREASE_BUFFER".equals(suggestedAction)) {
            return (int) Math.round(currentUnits * (1 + DEFAULT_INCREASE_PERCENTAGE / 100));
        } else if ("DECREASE_BUFFER".equals(suggestedAction)) {
            return (int) Math.round(currentUnits * (1 - DEFAULT_DECREASE_PERCENTAGE / 100));
        }
        return currentUnits; // Maintain if no change
    }

    private String determineTriggerReason(InventoryBuffer buffer, String suggestedAction) {
        if ("MAINTAIN_BUFFER".equals(suggestedAction)) {
            return "Within acceptable zone limits";
        } else if ("INCREASE_BUFFER".equals(suggestedAction)) {
            return String.format("In %s zone for %d days exceeding threshold of %d",
                    buffer.getCurrentZone(), buffer.getConsecutiveZoneDays(), getAdjustmentThresholdDays(buffer));
        } else {
            return String.format("In GREEN zone for %d days exceeding threshold of %d",
                    buffer.getConsecutiveZoneDays(), getAdjustmentThresholdDays(buffer));
        }
    }

    private int getAdjustmentThresholdDays(InventoryBuffer buffer) {
        return buffer.getAdjustmentThresholdDays() != null
                ? buffer.getAdjustmentThresholdDays()
                : DEFAULT_CONSECUTIVE_THRESHOLD_DAYS;
    }

    private LocalDateTime calculateNextReviewDue(InventoryBuffer buffer) {
        int reviewPeriod = buffer.getDbmReviewPeriodDays() != null
                ? buffer.getDbmReviewPeriodDays()
                : 30; // Default to 30 days if not set
        return LocalDateTime.now().plusDays(reviewPeriod);
    }

    private boolean isBufferLocked(InventoryBuffer buffer) {
        if (buffer.getLastReviewDate() == null) {
            return false;
        }
        int lockPeriodDays = (buffer.getDbmReviewPeriodDays() != null
                ? buffer.getDbmReviewPeriodDays()
                : 30) * DEFAULT_LOCK_PERIOD_CYCLES;
        LocalDateTime lockUntil = buffer.getLastReviewDate().plusDays(lockPeriodDays);
        return LocalDateTime.now().isBefore(lockUntil);
    }

    private Integer calculateSafetyBuffer(InventoryBuffer buffer) {
        // Placeholder: Use consumption profile or lead time variability for safety buffer
        // This should be refined based on actual business logic
        return buffer.getBufferUnits() != null ? (int) (buffer.getBufferUnits() * 0.1) : 0;
    }

    private String determineAdjustmentType(Integer oldBufferUnits, Integer newBufferUnits) {
        if (oldBufferUnits == null || newBufferUnits == null) return "MAINTAIN_BUFFER";
        if (newBufferUnits > oldBufferUnits) return "INCREASE_BUFFER";
        if (newBufferUnits < oldBufferUnits) return "DECREASE_BUFFER";
        return "MAINTAIN_BUFFER";
    }

    private String getProductName(String productId) {
        try {
            return productService.getProductById(productId).getName();
        } catch (Exception e) {
            log.warn("Product name not found for productId: {}", productId);
            return "Unknown";
        }
    }

    private String getLocationName(String locationId) {
        try {
            return locationService.getLocationById(locationId).getName();
        } catch (Exception e) {
            log.warn("Location name not found for locationId: {}", locationId);
            return "Unknown";
        }
    }
}