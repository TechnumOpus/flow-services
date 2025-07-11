package com.onified.distribute.service.impl;

import com.onified.distribute.dto.LeadTimeDTO;
import com.onified.distribute.entity.LeadTime;
import com.onified.distribute.repository.LeadTimeRepository;
import com.onified.distribute.service.LeadTimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LeadTimeServiceImpl implements LeadTimeService {

    private final LeadTimeRepository leadTimeRepository;


    @Override
     public LeadTimeDTO getLeadTimeByProductAndLocation(String productId, String locationId) {
        log.info("Fetching lead time for product: {} at location: {}", productId, locationId);
        
        LeadTime leadTime = leadTimeRepository.findByProductIdAndLocationIdAndIsActive(productId, locationId, true)
            .orElseThrow(() -> new IllegalArgumentException(
                "Active lead time not found for product: " + productId + " at location: " + locationId));
        
        return mapToDto(leadTime);
    }

    @Override
    public LeadTimeDTO createLeadTime(LeadTimeDTO leadTimeDto) {
        log.info("Creating lead time for product: {} at location: {}", 
                leadTimeDto.getProductId(), leadTimeDto.getLocationId());
        
        // Deactivate existing active lead time for same product-location
        leadTimeRepository.findByProductIdAndLocationIdAndIsActive(
            leadTimeDto.getProductId(), leadTimeDto.getLocationId(), true)
            .ifPresent(existing -> {
                existing.setIsActive(false);
                existing.setEffectiveTo(LocalDateTime.now());
                existing.setUpdatedAt(LocalDateTime.now());
                leadTimeRepository.save(existing);
                log.info("Deactivated existing lead time for product: {} at location: {}", 
                        leadTimeDto.getProductId(), leadTimeDto.getLocationId());
            });
        
        LeadTime leadTime = mapToEntity(leadTimeDto);
        leadTime.setEffectiveFrom(LocalDateTime.now());
        leadTime.setIsActive(true);
        leadTime.setUpdatedAt(LocalDateTime.now());
        
        LeadTime savedLeadTime = leadTimeRepository.save(leadTime);
        log.info("Lead time created successfully for product: {} at location: {}", 
                leadTimeDto.getProductId(), leadTimeDto.getLocationId());
        
        return mapToDto(savedLeadTime);
    }

    @Override
    public LeadTimeDTO updateLeadTime(String id, LeadTimeDTO leadTimeDto) {
        log.info("Updating lead time: {}", id);
        
        LeadTime existingLeadTime = leadTimeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Lead time not found: " + id));
        
        updateEntityFromDto(existingLeadTime, leadTimeDto);
        existingLeadTime.setUpdatedAt(LocalDateTime.now());
        
        LeadTime savedLeadTime = leadTimeRepository.save(existingLeadTime);
        log.info("Lead time updated successfully: {}", id);
        
        return mapToDto(savedLeadTime);
    }

    @Override
    @Transactional(readOnly = true)
    public LeadTimeDTO getLeadTimeById(String id) {
        LeadTime leadTime = leadTimeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Lead time not found: " + id));
        return mapToDto(leadTime);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadTimeDTO> getAllLeadTimes(Pageable pageable) {
        return leadTimeRepository.findAll(pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadTimeDTO> getActiveLeadTimes(Pageable pageable) {
        return leadTimeRepository.findByIsActive(true, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public LeadTimeDTO getActiveLeadTimeByProductAndLocation(String productId, String locationId) {
        LeadTime leadTime = leadTimeRepository.findByProductIdAndLocationIdAndIsActive(productId, locationId, true)
            .orElseThrow(() -> new IllegalArgumentException(
                "Active lead time not found for product: " + productId + " at location: " + locationId));
        return mapToDto(leadTime);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadTimeDTO> getLeadTimesByProduct(String productId, Pageable pageable) {
        return leadTimeRepository.findByProductId(productId, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadTimeDTO> getLeadTimesByLocation(String locationId, Pageable pageable) {
        return leadTimeRepository.findByLocationId(locationId, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeadTimeDTO> getLeadTimesByProductAndLocationIds(List<String> productIds, List<String> locationIds) {
        return leadTimeRepository.findByProductIdInAndLocationIdInAndIsActive(productIds, locationIds, true)
            .stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    @Override
    public LeadTimeDTO activateLeadTime(String id) {
        log.info("Activating lead time: {}", id);
        
        LeadTime leadTime = leadTimeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Lead time not found: " + id));
        
        // Deactivate other active lead times for same product-location
        leadTimeRepository.findByProductIdAndLocationIdAndIsActive(
            leadTime.getProductId(), leadTime.getLocationId(), true)
            .ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    existing.setIsActive(false);
                    existing.setEffectiveTo(LocalDateTime.now());
                    existing.setUpdatedAt(LocalDateTime.now());
                    leadTimeRepository.save(existing);
                }
            });
        
        leadTime.setIsActive(true);
        leadTime.setEffectiveFrom(LocalDateTime.now());
        leadTime.setEffectiveTo(null);
        leadTime.setUpdatedAt(LocalDateTime.now());
        
        LeadTime savedLeadTime = leadTimeRepository.save(leadTime);
        log.info("Lead time activated successfully: {}", id);
        
        return mapToDto(savedLeadTime);
    }

    @Override
    public LeadTimeDTO deactivateLeadTime(String id) {
        log.info("Deactivating lead time: {}", id);
        
        LeadTime leadTime = leadTimeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Lead time not found: " + id));
        
        leadTime.setIsActive(false);
        leadTime.setEffectiveTo(LocalDateTime.now());
        leadTime.setUpdatedAt(LocalDateTime.now());
        
        LeadTime savedLeadTime = leadTimeRepository.save(leadTime);
        log.info("Lead time deactivated successfully: {}", id);
        
        return mapToDto(savedLeadTime);
    }

    @Override
    public void deleteLeadTime(String id) {
        log.info("Deleting lead time: {}", id);
        
        LeadTime leadTime = leadTimeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Lead time not found: " + id));
        
        leadTimeRepository.delete(leadTime);
        log.info("Lead time deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateTotalLeadTime(String productId, String locationId) {
        LeadTime leadTime = leadTimeRepository.findByProductIdAndLocationIdAndIsActive(productId, locationId, true)
            .orElse(null);
        
        if (leadTime == null) {
            log.warn("No active lead time found for product: {} at location: {}", productId, locationId);
            return 0.0;
        }
        
        double totalLeadTime = 0.0;
        if (leadTime.getOrderLeadTime() != null) {
            totalLeadTime += leadTime.getOrderLeadTime();
        }
        if (leadTime.getManufacturingTime() != null) {
            totalLeadTime += leadTime.getManufacturingTime();
        }
        if (leadTime.getTransportTime() != null) {
            totalLeadTime += leadTime.getTransportTime();
        }

        
        return totalLeadTime;
    }

    private LeadTime mapToEntity(LeadTimeDTO dto) {
        LeadTime leadTime = new LeadTime();
        leadTime.setProductId(dto.getProductId());
        leadTime.setLocationId(dto.getLocationId());
        leadTime.setSupplierId(dto.getSupplierId());
        leadTime.setOrderLeadTime(dto.getOrderLeadTime());
        leadTime.setManufacturingTime(dto.getManufacturingTime());
        leadTime.setTransportTime(dto.getTransportTime());
        leadTime.setLeadTimeVariability(dto.getLeadTimeVariability());
        leadTime.setOnTimeDeliveryPct(dto.getOnTimeDeliveryPct());
        leadTime.setUpdatedBy(dto.getUpdatedBy());
        return leadTime;
    }

    private LeadTimeDTO mapToDto(LeadTime entity) {
        LeadTimeDTO dto = new LeadTimeDTO();
        dto.setId(entity.getId());
        dto.setProductId(entity.getProductId());
        dto.setLocationId(entity.getLocationId());
        dto.setSupplierId(entity.getSupplierId());
        dto.setOrderLeadTime(entity.getOrderLeadTime());
        dto.setManufacturingTime(entity.getManufacturingTime());
        dto.setTransportTime(entity.getTransportTime());
        dto.setLeadTimeVariability(entity.getLeadTimeVariability());
        dto.setOnTimeDeliveryPct(entity.getOnTimeDeliveryPct());
        dto.setEffectiveFrom(entity.getEffectiveFrom());
        dto.setEffectiveTo(entity.getEffectiveTo());
        dto.setIsActive(entity.getIsActive());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setUpdatedBy(entity.getUpdatedBy());
        return dto;
    }

    private void updateEntityFromDto(LeadTime entity, LeadTimeDTO dto) {
        entity.setSupplierId(dto.getSupplierId());
        entity.setOrderLeadTime(dto.getOrderLeadTime());
        entity.setManufacturingTime(dto.getManufacturingTime());
        entity.setTransportTime(dto.getTransportTime());
        entity.setLeadTimeVariability(dto.getLeadTimeVariability());
        entity.setOnTimeDeliveryPct(dto.getOnTimeDeliveryPct());
        entity.setUpdatedBy(dto.getUpdatedBy());
    }
}
