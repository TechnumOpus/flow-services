package com.onified.distribute.service.impl.dbm;

import com.onified.distribute.dto.InventoryBufferDTO;
import com.onified.distribute.entity.InventoryBuffer;
import com.onified.distribute.repository.InventoryBufferRepository;
import com.onified.distribute.service.dbm.BufferAdjustmentLogService;
import com.onified.distribute.service.dbm.InventoryBufferService;
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
public class InventoryBufferServiceImpl implements InventoryBufferService {

    private final InventoryBufferRepository inventoryBufferRepository;
    private final BufferAdjustmentLogService bufferAdjustmentLogService;

    @Override
    public InventoryBufferDTO getBufferByProductAndLocation(String productId, String locationId) {
        return inventoryBufferRepository.findByProductIdAndLocationId(productId, locationId)
                .map(this::convertToDto)
                .orElse(null);
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

        existingBuffer = calculateZone(existingBuffer);

        InventoryBuffer savedBuffer = inventoryBufferRepository.save(existingBuffer);
        return convertToDto(savedBuffer);
    }

    @Override
    public void deactivateBufferByProductAndLocation(String productId, String locationId) {
        log.info("Deactivating buffer for product: {} at location: {}", productId, locationId);
        InventoryBuffer buffer = inventoryBufferRepository.findByProductIdAndLocationId(productId, locationId)
                .orElse(null);
        if (buffer != null) {
            buffer.setIsActive(false);
            buffer.setUpdatedAt(LocalDateTime.now());
            inventoryBufferRepository.save(buffer);
            log.info("Buffer deactivated successfully for product: {} at location: {}", productId, locationId);
        }
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

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryBufferDTO> getInventoryBuffersByProductIds(List<String> productIds, Pageable pageable) {
        log.info("Fetching inventory buffers for product IDs: {}", productIds);
        return inventoryBufferRepository.findByProductIdIn(productIds, pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryBufferDTO> getInventoryBuffersByProductIdsAndCurrentZone(List<String> productIds, String currentZone, Pageable pageable) {
        log.info("Fetching inventory buffers for product IDs: {} and current zone: {}", productIds, currentZone);
        return inventoryBufferRepository.findByProductIdInAndCurrentZone(productIds, currentZone, pageable).map(this::convertToDto);
    }

    private InventoryBuffer calculateZone(InventoryBuffer buffer) {
        if (buffer.getBufferUnits() == null || buffer.getBufferUnits() == 0) {
            buffer.setCurrentZone("GREEN");
            buffer.setBufferConsumedPct(0.0);
            return buffer;
        }

        Integer netAvailable = buffer.getNetAvailableQty() != null ? buffer.getNetAvailableQty() :
                (buffer.getCurrentInventory() != null ? buffer.getCurrentInventory() : 0) +
                        (buffer.getInPipelineQty() != null ? buffer.getInPipelineQty() : 0);

        buffer.setNetAvailableQty(netAvailable);

        double consumedPct = ((double) (buffer.getBufferUnits() - netAvailable) / buffer.getBufferUnits()) * 100;
        buffer.setBufferConsumedPct(Math.max(0, Math.min(100, consumedPct)));

        // Use threshold percentages for zone determination
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