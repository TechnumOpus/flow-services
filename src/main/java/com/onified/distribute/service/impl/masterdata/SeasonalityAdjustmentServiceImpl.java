package com.onified.distribute.service.impl.masterdata;

import com.onified.distribute.dto.SeasonalityAdjustmentDTO;
import com.onified.distribute.dto.SeasonalityMatrixResponseDTO;
import com.onified.distribute.entity.SeasonalityAdjustment;
import com.onified.distribute.repository.SeasonalityAdjustmentRepository;
import com.onified.distribute.service.masterdata.SeasonalityAdjustmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SeasonalityAdjustmentServiceImpl implements SeasonalityAdjustmentService {

    private final SeasonalityAdjustmentRepository seasonalityAdjustmentRepository;

    @Override
    public SeasonalityAdjustmentDTO createSeasonalityAdjustment(SeasonalityAdjustmentDTO adjustmentDto) {
        log.info("Creating seasonality adjustment for product: {} at location: {} for month: {}",
                adjustmentDto.getProductId(), adjustmentDto.getLocationId(), adjustmentDto.getMonth());

        if (adjustmentDto.getAdjustmentId() != null &&
                seasonalityAdjustmentRepository.existsByAdjustmentId(adjustmentDto.getAdjustmentId())) {
            throw new IllegalArgumentException("Adjustment ID already exists: " + adjustmentDto.getAdjustmentId());
        }

        SeasonalityAdjustment adjustment = mapToEntity(adjustmentDto);
        adjustment.setCreatedAt(LocalDateTime.now());
        adjustment.setUpdatedAt(LocalDateTime.now());

        if (adjustment.getAdjustmentId() == null) {
            adjustment.setAdjustmentId("ADJ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        SeasonalityAdjustment savedAdjustment = seasonalityAdjustmentRepository.save(adjustment);
        log.info("Seasonality adjustment created successfully with ID: {}", savedAdjustment.getAdjustmentId());
        return mapToDto(savedAdjustment);
    }

    @Override
    @Transactional(readOnly = true)
    public SeasonalityAdjustmentDTO getSeasonalityAdjustmentById(String adjustmentId) {
        SeasonalityAdjustment adjustment = seasonalityAdjustmentRepository.findByAdjustmentId(adjustmentId)
                .orElseThrow(() -> new IllegalArgumentException("Seasonality adjustment not found: " + adjustmentId));
        return mapToDto(adjustment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SeasonalityAdjustmentDTO> getAllSeasonalityAdjustments(Pageable pageable) {
        return seasonalityAdjustmentRepository.findAll(pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SeasonalityAdjustmentDTO> getActiveSeasonalityAdjustments(Pageable pageable) {
        return seasonalityAdjustmentRepository.findByIsActive(true, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SeasonalityAdjustmentDTO> getSeasonalityAdjustmentsByProduct(String productId, Pageable pageable) {
        return seasonalityAdjustmentRepository.findByProductId(productId, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SeasonalityAdjustmentDTO> getSeasonalityAdjustmentsByLocation(String locationId, Pageable pageable) {
        return seasonalityAdjustmentRepository.findByLocationId(locationId, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SeasonalityAdjustmentDTO> getSeasonalityAdjustmentsByMonth(Integer month, Pageable pageable) {
        return seasonalityAdjustmentRepository.findByMonth(month, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeasonalityAdjustmentDTO> getSeasonalityAdjustmentsByProductAndLocation(String productId, String locationId) {
        return seasonalityAdjustmentRepository.findByProductIdAndLocationIdAndIsActive(productId, locationId, true)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double getSeasonalityFactor(String productId, String locationId, Integer month) {
        return seasonalityAdjustmentRepository.findByProductIdAndLocationIdAndMonthAndIsActive(
                        productId, locationId, month, true)
                .map(SeasonalityAdjustment::getSeasonalityFactor)
                .orElse(1.0); // Default factor if no adjustment found
    }

    @Override
    public SeasonalityAdjustmentDTO activateSeasonalityAdjustment(String adjustmentId) {
        log.info("Activating seasonality adjustment: {}", adjustmentId);
        SeasonalityAdjustment adjustment = seasonalityAdjustmentRepository.findByAdjustmentId(adjustmentId)
                .orElseThrow(() -> new IllegalArgumentException("Seasonality adjustment not found: " + adjustmentId));

        adjustment.setIsActive(true);
        adjustment.setUpdatedAt(LocalDateTime.now());

        SeasonalityAdjustment savedAdjustment = seasonalityAdjustmentRepository.save(adjustment);
        log.info("Seasonality adjustment activated successfully: {}", adjustmentId);
        return mapToDto(savedAdjustment);
    }

    @Override
    public SeasonalityAdjustmentDTO deactivateSeasonalityAdjustment(String adjustmentId) {
        log.info("Deactivating seasonality adjustment: {}", adjustmentId);
        SeasonalityAdjustment adjustment = seasonalityAdjustmentRepository.findByAdjustmentId(adjustmentId)
                .orElseThrow(() -> new IllegalArgumentException("Seasonality adjustment not found: " + adjustmentId));

        adjustment.setIsActive(false);
        adjustment.setUpdatedAt(LocalDateTime.now());

        SeasonalityAdjustment savedAdjustment = seasonalityAdjustmentRepository.save(adjustment);
        log.info("Seasonality adjustment deactivated successfully: {}", adjustmentId);
        return mapToDto(savedAdjustment);
    }

    @Override
    public void deleteSeasonalityAdjustment(String adjustmentId) {
        log.info("Deleting seasonality adjustment: {}", adjustmentId);
        SeasonalityAdjustment adjustment = seasonalityAdjustmentRepository.findByAdjustmentId(adjustmentId)
                .orElseThrow(() -> new IllegalArgumentException("Seasonality adjustment not found: " + adjustmentId));

        seasonalityAdjustmentRepository.delete(adjustment);
        log.info("Seasonality adjustment deleted successfully: {}", adjustmentId);
    }

    @Override
    public SeasonalityAdjustmentDTO updateSeasonalityAdjustment(String adjustmentId, SeasonalityAdjustmentDTO adjustmentDto) {
        log.info("Updating seasonality adjustment: {}", adjustmentId);
        SeasonalityAdjustment adjustment = seasonalityAdjustmentRepository.findByAdjustmentId(adjustmentId)
                .orElseThrow(() -> new IllegalArgumentException("Seasonality adjustment not found: " + adjustmentId));

        updateEntityFromDto(adjustment, adjustmentDto);
        adjustment.setUpdatedAt(LocalDateTime.now());

        SeasonalityAdjustment savedAdjustment = seasonalityAdjustmentRepository.save(adjustment);
        log.info("Seasonality adjustment updated successfully: {}", adjustmentId);
        return mapToDto(savedAdjustment);
    }

    // In the getSeasonalityMatrix method, around line 166:

    @Override
    @Transactional(readOnly = true)
    public Page<SeasonalityMatrixResponseDTO> getSeasonalityMatrix(String type, String locationId, String category,
                                                                   String productId, Integer year, Boolean isActive, Pageable pageable) {
        log.info("Fetching seasonality matrix with filters: type={}, locationId={}, category={}, productId={}, year={}, isActive={}",
                type, locationId, category, productId, year, isActive);

        // Handle "ALL" type filter
        String typeFilter = (type != null && type.equals("ALL")) ? null : type;

        List<SeasonalityAdjustment> adjustments = seasonalityAdjustmentRepository.findSeasonalityMatrixData(
                typeFilter, locationId, category, productId, year, isActive);

        // Group adjustments by location, type, and typeName
        Map<String, List<SeasonalityAdjustment>> groupedAdjustments = adjustments.stream()
                .collect(Collectors.groupingBy(this::createGroupingKey));

        // Convert to matrix response format
        List<SeasonalityMatrixResponseDTO> matrixResponses = groupedAdjustments.entrySet().stream()
                .map(entry -> convertToMatrixResponse(entry.getValue()))
                .collect(Collectors.toList());

        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), matrixResponses.size());

        if (start >= matrixResponses.size()) {
            return new PageImpl<>(List.of(), pageable, matrixResponses.size());
        }

        List<SeasonalityMatrixResponseDTO> pagedList = matrixResponses.subList(start, end);

        return new PageImpl<>(pagedList, pageable, matrixResponses.size());
    }


    @Override
    public List<SeasonalityAdjustmentDTO> updateSeasonalityBulk(List<SeasonalityAdjustmentDTO> adjustments) {
        log.info("Updating seasonality adjustments in bulk for {} entries", adjustments.size());
        return adjustments.stream().map(adjustment -> {
            if (adjustment.getAdjustmentId() == null) {
                return createSeasonalityAdjustment(adjustment);
            } else {
                return updateSeasonalityAdjustment(adjustment.getAdjustmentId(), adjustment);
            }
        }).collect(Collectors.toList());
    }

    // Helper method to create grouping key for matrix response
    private String createGroupingKey(SeasonalityAdjustment adjustment) {
        String location = adjustment.getLocationId() != null ? adjustment.getLocationId() : "ALL";
        String type = adjustment.getType() != null ? adjustment.getType() : "ALL";
        String typeName = getTypeName(adjustment);
        return location + "|" + type + "|" + typeName;
    }

    // Helper method to get typeName based on type
    private String getTypeName(SeasonalityAdjustment adjustment) {
        if (adjustment.getType() == null) {
            return null;
        }

        switch (adjustment.getType().toUpperCase()) {
            case "SKU":
                return adjustment.getProductId();
            case "CATEGORY":
            case "SUB CATEGORY":
                return adjustment.getCategory();
            default:
                return null;
        }
    }

    // Helper method to convert grouped adjustments to matrix response
    private SeasonalityMatrixResponseDTO convertToMatrixResponse(List<SeasonalityAdjustment> adjustments) {
        if (adjustments.isEmpty()) {
            return new SeasonalityMatrixResponseDTO();
        }

        SeasonalityAdjustment firstAdjustment = adjustments.get(0);
        SeasonalityMatrixResponseDTO response = new SeasonalityMatrixResponseDTO();

        response.setLocation(firstAdjustment.getLocationId() != null ? firstAdjustment.getLocationId() : "ALL");
        response.setType(firstAdjustment.getType() != null ? firstAdjustment.getType() : "ALL");
        response.setTypeName(getTypeName(firstAdjustment));

        // Create seasonality factors map
        Map<String, Double> seasonalityFactors = new HashMap<>();
        for (SeasonalityAdjustment adjustment : adjustments) {
            if (adjustment.getYear() != null && adjustment.getMonth() != null && adjustment.getSeasonalityFactor() != null) {
                String key = String.format("%d-%02d", adjustment.getYear(), adjustment.getMonth());
                seasonalityFactors.put(key, adjustment.getSeasonalityFactor());
            }
        }

        response.setSeasonalityFactors(seasonalityFactors);
        return response;
    }

    private SeasonalityAdjustment mapToEntity(SeasonalityAdjustmentDTO dto) {
        SeasonalityAdjustment adjustment = new SeasonalityAdjustment();
        adjustment.setAdjustmentId(dto.getAdjustmentId());
        adjustment.setProductId(dto.getProductId());
        adjustment.setLocationId(dto.getLocationId());
        adjustment.setCategory(dto.getCategory());
        adjustment.setMonth(dto.getMonth());
        adjustment.setSeasonalityFactor(dto.getSeasonalityFactor());
        adjustment.setYear(dto.getYear());
        adjustment.setDescription(dto.getDescription());
        adjustment.setConfidenceLevel(dto.getConfidenceLevel());
        adjustment.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        adjustment.setCreatedBy(dto.getCreatedBy());
        adjustment.setUpdatedBy(dto.getUpdatedBy());
        adjustment.setType(dto.getType());
        return adjustment;
    }

    private SeasonalityAdjustmentDTO mapToDto(SeasonalityAdjustment entity) {
        SeasonalityAdjustmentDTO dto = new SeasonalityAdjustmentDTO();
        dto.setId(entity.getId());
        dto.setAdjustmentId(entity.getAdjustmentId());
        dto.setProductId(entity.getProductId());
        dto.setLocationId(entity.getLocationId());
        dto.setCategory(entity.getCategory());
        dto.setMonth(entity.getMonth());
        dto.setSeasonalityFactor(entity.getSeasonalityFactor());
        dto.setYear(entity.getYear());
        dto.setDescription(entity.getDescription());
        dto.setConfidenceLevel(entity.getConfidenceLevel());
        dto.setIsActive(entity.getIsActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setType(entity.getType());
        return dto;
    }

    private void updateEntityFromDto(SeasonalityAdjustment entity, SeasonalityAdjustmentDTO dto) {
        if (dto.getProductId() != null) {
            entity.setProductId(dto.getProductId());
        }
        if (dto.getLocationId() != null) {
            entity.setLocationId(dto.getLocationId());
        }
        if (dto.getCategory() != null) {
            entity.setCategory(dto.getCategory());
        }
        if (dto.getMonth() != null) {
            entity.setMonth(dto.getMonth());
        }
        if (dto.getSeasonalityFactor() != null) {
            entity.setSeasonalityFactor(dto.getSeasonalityFactor());
        }
        if (dto.getYear() != null) {
            entity.setYear(dto.getYear());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getConfidenceLevel() != null) {
            entity.setConfidenceLevel(dto.getConfidenceLevel());
        }
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }
        if (dto.getType() != null) {
            entity.setType(dto.getType());
        }
        if (dto.getUpdatedBy() != null) {
            entity.setUpdatedBy(dto.getUpdatedBy());
        }
    }
}
