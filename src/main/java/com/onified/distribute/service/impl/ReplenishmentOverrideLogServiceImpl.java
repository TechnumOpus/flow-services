package com.onified.distribute.service.impl;

import com.onified.distribute.dto.ReplenishmentOverrideLogDTO;
import com.onified.distribute.entity.ReplenishmentOverrideLog;
import com.onified.distribute.repository.ReplenishmentOverrideLogRepository;
import com.onified.distribute.service.ReplenishmentOverrideLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReplenishmentOverrideLogServiceImpl implements ReplenishmentOverrideLogService {

    private final ReplenishmentOverrideLogRepository overrideLogRepository;

    @Override
    public void createOverrideLog(ReplenishmentOverrideLogDTO overrideLogDTO) {
        log.info("Creating override log for product: {}, location: {}", overrideLogDTO.getProductId(), overrideLogDTO.getLocationId());
        ReplenishmentOverrideLog overrideLog = mapToEntity(overrideLogDTO);
        overrideLog.setTimestamp(LocalDateTime.now());
        ReplenishmentOverrideLog savedOverrideLog = overrideLogRepository.save(overrideLog);
        convertToDto(savedOverrideLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReplenishmentOverrideLogDTO> getAllOverrideLogs(Pageable pageable) {
        log.info("Fetching all override logs");
        return overrideLogRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReplenishmentOverrideLogDTO> getOverrideLogsByProductId(String productId, Pageable pageable) {
        log.info("Fetching override logs for product: {}", productId);
        return overrideLogRepository.findByProductId(productId, pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReplenishmentOverrideLogDTO> getOverrideLogsByLocationId(String locationId, Pageable pageable) {
        log.info("Fetching override logs for location: {}", locationId);
        return overrideLogRepository.findByLocationId(locationId, pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReplenishmentOverrideLogDTO> getOverrideLogsByProductIdAndLocationId(String productId, String locationId, Pageable pageable) {
        log.info("Fetching override logs for product: {} and location: {}", productId, locationId);
        return overrideLogRepository.findByProductIdAndLocationId(productId, locationId, pageable).map(this::convertToDto);
    }

    private ReplenishmentOverrideLogDTO convertToDto(ReplenishmentOverrideLog overrideLog) {
        ReplenishmentOverrideLogDTO dto = new ReplenishmentOverrideLogDTO();
        dto.setId(overrideLog.getId());
        dto.setProductId(overrideLog.getProductId());
        dto.setLocationId(overrideLog.getLocationId());
        dto.setOriginalQuantity(overrideLog.getOriginalQuantity());
        dto.setOverriddenQuantity(overrideLog.getOverriddenQuantity());
        dto.setReason(overrideLog.getReason());
        dto.setApprover(overrideLog.getApprover());
        dto.setTimestamp(overrideLog.getTimestamp());
        dto.setCreatedBy(overrideLog.getCreatedBy());
        return dto;
    }

    private ReplenishmentOverrideLog mapToEntity(ReplenishmentOverrideLogDTO dto) {
        ReplenishmentOverrideLog overrideLog = new ReplenishmentOverrideLog();
        overrideLog.setProductId(dto.getProductId());
        overrideLog.setLocationId(dto.getLocationId());
        overrideLog.setOriginalQuantity(dto.getOriginalQuantity());
        overrideLog.setOverriddenQuantity(dto.getOverriddenQuantity());
        overrideLog.setReason(dto.getReason());
        overrideLog.setApprover(dto.getApprover());
        overrideLog.setTimestamp(dto.getTimestamp());
        overrideLog.setCreatedBy(dto.getCreatedBy());
        return overrideLog;
    }
}