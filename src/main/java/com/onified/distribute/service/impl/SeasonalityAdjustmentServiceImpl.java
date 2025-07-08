package com.onified.distribute.service.impl;

import com.onified.distribute.dto.SeasonalityAdjustmentDTO;
import com.onified.distribute.entity.SeasonalityAdjustment;
import com.onified.distribute.repository.SeasonalityAdjustmentRepository;
import com.onified.distribute.service.SeasonalityAdjustmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
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
        return dto;
    }

    private void updateEntityFromDto(SeasonalityAdjustment entity, SeasonalityAdjustmentDTO dto) {
        entity.setProductId(dto.getProductId());
        entity.setLocationId(dto.getLocationId());
        entity.setCategory(dto.getCategory());
        entity.setMonth(dto.getMonth());
        entity.setSeasonalityFactor(dto.getSeasonalityFactor());
        entity.setYear(dto.getYear());
        entity.setDescription(dto.getDescription());
        entity.setConfidenceLevel(dto.getConfidenceLevel());
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }
        entity.setUpdatedBy(dto.getUpdatedBy());
    }
}