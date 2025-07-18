package com.onified.distribute.service.impl.masterdata;

import com.onified.distribute.dto.InventoryBufferDTO;
import com.onified.distribute.dto.LeadTimeDTO;
import com.onified.distribute.dto.response.LeadTimeResponseDTO;
import com.onified.distribute.dto.request.LeadTimeUpdateDTO;
import com.onified.distribute.entity.ConsumptionProfile;
import com.onified.distribute.entity.LeadTime;
import com.onified.distribute.entity.Product;
import com.onified.distribute.entity.Supplier;
import com.onified.distribute.entity.Location;
import com.onified.distribute.repository.ConsumptionProfileRepository;
import com.onified.distribute.repository.LeadTimeRepository;
import com.onified.distribute.repository.ProductRepository;
import com.onified.distribute.repository.SupplierRepository;
import com.onified.distribute.repository.LocationRepository;
import com.onified.distribute.service.dbm.InventoryBufferService;
import com.onified.distribute.service.masterdata.LeadTimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LeadTimeServiceImpl implements LeadTimeService {

    private final LeadTimeRepository leadTimeRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final LocationRepository locationRepository;
    private final ConsumptionProfileRepository consumptionProfileRepository;
    private final InventoryBufferService inventoryBufferService;

    @Override
    public LeadTimeDTO createLeadTime(LeadTimeDTO leadTimeDto) {
        log.info("Creating lead time for product: {} at location: {}", leadTimeDto.getProductId(), leadTimeDto.getLocationId());

        // Validate product exists
        productRepository.findByProductId(leadTimeDto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + leadTimeDto.getProductId()));

        // Validate location exists
        locationRepository.findByLocationId(leadTimeDto.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + leadTimeDto.getLocationId()));

        // Validate supplier if provided
        if (leadTimeDto.getSupplierId() != null) {
            supplierRepository.findById(leadTimeDto.getSupplierId())
                    .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + leadTimeDto.getSupplierId()));
        }

        // Deactivate existing active lead time for same product-location
        leadTimeRepository.findByProductIdAndLocationIdAndIsActive(
                        leadTimeDto.getProductId(), leadTimeDto.getLocationId(), true)
                .ifPresent(existing -> {
                    existing.setIsActive(false);
                    existing.setUpdatedAt(LocalDateTime.now());
                    leadTimeRepository.save(existing);
                    log.info("Deactivated existing lead time for product: {} at location: {}",
                            leadTimeDto.getProductId(), leadTimeDto.getLocationId());
                });

        LeadTime leadTime = mapToEntity(leadTimeDto);
        leadTime.setIsActive(true);
        leadTime.setCreatedAt(LocalDateTime.now());
        leadTime.setUpdatedAt(LocalDateTime.now());

        LeadTime savedLeadTime = leadTimeRepository.save(leadTime);

        // Process buffer creation/update
        processBufferForLeadTime(savedLeadTime);

        log.info("Lead time created successfully for product: {} at location: {}",
                leadTimeDto.getProductId(), leadTimeDto.getLocationId());
        return mapToDto(savedLeadTime);
    }

    @Override
    public LeadTimeDTO updateLeadTime(String id, LeadTimeDTO leadTimeDto) {
        log.info("Updating lead time: {}", id);
        LeadTime existingLeadTime = leadTimeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead time not found: " + id));

        // Update only lead time fields and moq
        existingLeadTime.setOrderLeadTime(leadTimeDto.getOrderLeadTime());
        existingLeadTime.setManufacturingTime(leadTimeDto.getManufacturingTime());
        existingLeadTime.setTransportTime(leadTimeDto.getTransportTime());
        existingLeadTime.setMoq(leadTimeDto.getMoq());
        existingLeadTime.setUpdatedAt(LocalDateTime.now());

        LeadTime savedLeadTime = leadTimeRepository.save(existingLeadTime);

        // Process buffer update
        processBufferForLeadTime(savedLeadTime);

        return mapToDto(savedLeadTime);
    }

    @Override
    public LeadTimeResponseDTO updateLeadTimeFields(String id, LeadTimeUpdateDTO updateDto) {
        log.info("Updating lead time fields: {}", id);
        LeadTime leadTime = leadTimeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead time not found: " + id));

        boolean leadTimeChanged = false;

        // Update supplier if provided
        if (updateDto.getSupplierName() != null) {
            Supplier supplier = supplierRepository.findBySupplierName(updateDto.getSupplierName())
                    .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + updateDto.getSupplierName()));
            leadTime.setSupplierId(supplier.getSupplierId());
        }

        // Update MOQ in Product if provided
        if (updateDto.getMoq() != null) {
            Product product = productRepository.findByProductId(leadTime.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + leadTime.getProductId()));
            product.setUpdatedAt(LocalDateTime.now());
            product.setUpdatedBy(updateDto.getUpdatedBy());
            productRepository.save(product);
        }

        // Update lead time fields and track if any lead time component changed
        if (updateDto.getOrderLeadTime() != null) {
            leadTime.setOrderLeadTime(updateDto.getOrderLeadTime());
            leadTimeChanged = true;
        }
        if (updateDto.getManufacturingTime() != null) {
            leadTime.setManufacturingTime(updateDto.getManufacturingTime());
            leadTimeChanged = true;
        }
        if (updateDto.getTransportTime() != null) {
            leadTime.setTransportTime(updateDto.getTransportTime());
            leadTimeChanged = true;
        }

        leadTime.setUpdatedAt(LocalDateTime.now());
        leadTime.setUpdatedBy(updateDto.getUpdatedBy());

        LeadTime savedLeadTime = leadTimeRepository.save(leadTime);

        // Process buffer update if lead time components changed
        if (leadTimeChanged) {
            processBufferForLeadTime(savedLeadTime);
        }

        log.info("Lead time fields updated successfully: {}", id);
        return enrichLeadTimeData(savedLeadTime);
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
                        existing.setUpdatedAt(LocalDateTime.now());
                        leadTimeRepository.save(existing);
                    }
                });

        leadTime.setIsActive(true);
        leadTime.setUpdatedAt(LocalDateTime.now());

        LeadTime savedLeadTime = leadTimeRepository.save(leadTime);

        // Process buffer creation/update
        processBufferForLeadTime(savedLeadTime);

        log.info("Lead time activated successfully: {}", id);
        return mapToDto(savedLeadTime);
    }

    @Override
    public LeadTimeDTO deactivateLeadTime(String id) {
        log.info("Deactivating lead time: {}", id);
        LeadTime leadTime = leadTimeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead time not found: " + id));

        leadTime.setIsActive(false);
        leadTime.setUpdatedAt(LocalDateTime.now());

        LeadTime savedLeadTime = leadTimeRepository.save(leadTime);

        // Optionally deactivate the corresponding buffer
        try {
            inventoryBufferService.deactivateBufferByProductAndLocation(leadTime.getProductId(), leadTime.getLocationId());
            log.info("Buffer deactivated for deactivated lead time: {}", id);
        } catch (Exception e) {
            log.error("Failed to deactivate buffer for deactivated lead time: {}. Error: {}", id, e.getMessage());
        }

        log.info("Lead time deactivated successfully: {}", id);
        return mapToDto(savedLeadTime);
    }

    // Buffer processing logic
    private void processBufferForLeadTime(LeadTime leadTime) {
        String productId = leadTime.getProductId();
        String locationId = leadTime.getLocationId();

        log.info("Processing buffer for product: {} and location: {}", productId, locationId);

        // Fetch consumption profile for 7ADC
        ConsumptionProfile consumptionProfile = consumptionProfileRepository
                .findByProductIdAndLocationId(productId, locationId)
                .orElse(null);

        if (consumptionProfile == null) {
            log.warn("No consumption profile found for product: {} and location: {}. Skipping buffer processing.", productId, locationId);
            return;
        }

        Double sevenAdc = consumptionProfile.getAdc7d() != null ? consumptionProfile.getAdc7d() : 0.0;

        // Calculate buffer days
        double totalLeadTimeDays = calculateTotalLeadTimeDays(leadTime);
        int bufferDays = (int) Math.ceil(totalLeadTimeDays);

        // Calculate buffer units
        int bufferUnits = (int) Math.ceil(sevenAdc * bufferDays);

        // Check if buffer exists
        if (inventoryBufferService.existsByProductAndLocation(productId, locationId)) {
            log.info("Buffer exists for product: {} and location: {}. Updating...", productId, locationId);
            updateExistingBuffer(leadTime, bufferDays, bufferUnits);
        } else {
            log.info("Creating new buffer for product: {} and location: {}", productId, locationId);
            createNewBuffer(leadTime, bufferDays, bufferUnits, sevenAdc);
        }
    }

    private void createNewBuffer(LeadTime leadTime, int bufferDays, int bufferUnits, double sevenAdc) {
        try {
            InventoryBufferDTO bufferDto = new InventoryBufferDTO();
            bufferDto.setBufferId(UUID.randomUUID().toString());
            bufferDto.setProductId(leadTime.getProductId());
            bufferDto.setLocationId(leadTime.getLocationId());
            bufferDto.setBufferType("SAFETY");
            bufferDto.setBufferDays(bufferDays);
            bufferDto.setBufferUnits(bufferUnits);
            bufferDto.setGreenThresholdPct(33.0);
            bufferDto.setYellowThresholdPct(33.0);
            bufferDto.setRedThresholdPct(33.0);
            bufferDto.setCurrentInventory(0);
            bufferDto.setInPipelineQty(0);
            bufferDto.setNetAvailableQty(0);
            bufferDto.setBufferConsumedPct(0.0);
            bufferDto.setCurrentZone("GREEN");
            bufferDto.setReviewCycleId("CYCLE001");
            bufferDto.setDbmReviewPeriodDays(7); // Default review period
            bufferDto.setNextReviewDue(LocalDateTime.now().plusDays(7));
            bufferDto.setConsecutiveZoneDays(0);
            bufferDto.setAdjustmentThresholdDays(0);
            bufferDto.setIsActive(true);
            bufferDto.setCreatedAt(LocalDateTime.now());
            bufferDto.setUpdatedAt(LocalDateTime.now());
            bufferDto.setCreatedBy(leadTime.getCreatedBy());
            bufferDto.setUpdatedBy(leadTime.getUpdatedBy());

            inventoryBufferService.createInventoryBuffer(bufferDto);
            log.info("Successfully created buffer for product: {} at location: {}", leadTime.getProductId(), leadTime.getLocationId());
        } catch (Exception e) {
            log.error("Failed to create buffer for product: {} at location: {}. Error: {}",
                    leadTime.getProductId(), leadTime.getLocationId(), e.getMessage());
        }
    }

    private void updateExistingBuffer(LeadTime leadTime, int bufferDays, int bufferUnits) {
        try {
            InventoryBufferDTO existingBuffer = inventoryBufferService.getBufferByProductAndLocation(leadTime.getProductId(), leadTime.getLocationId());
            existingBuffer.setBufferDays(bufferDays);
            existingBuffer.setBufferUnits(bufferUnits);
            existingBuffer.setGreenThresholdPct(33.0);
            existingBuffer.setYellowThresholdPct(33.0);
            existingBuffer.setRedThresholdPct(33.0);
            existingBuffer.setUpdatedAt(LocalDateTime.now());
            existingBuffer.setUpdatedBy(leadTime.getUpdatedBy());

            // Recalculate net available quantity and zone
            int netAvailableQty = (existingBuffer.getCurrentInventory() != null ? existingBuffer.getCurrentInventory() : 0) +
                    (existingBuffer.getInPipelineQty() != null ? existingBuffer.getInPipelineQty() : 0);
            existingBuffer.setNetAvailableQty(netAvailableQty);

            double bufferConsumedPct = bufferUnits > 0 ?
                    ((double) (bufferUnits - netAvailableQty) / bufferUnits) * 100 : 0.0;
            existingBuffer.setBufferConsumedPct(Math.max(0.0, bufferConsumedPct));

            String currentZone = determineZone(netAvailableQty, bufferUnits);
            existingBuffer.setCurrentZone(currentZone);

            inventoryBufferService.updateInventoryBuffer(existingBuffer.getBufferId(), existingBuffer);
            log.info("Successfully updated buffer for product: {} at location: {}", leadTime.getProductId(), leadTime.getLocationId());
        } catch (Exception e) {
            log.error("Failed to update buffer for product: {} at location: {}. Error: {}",
                    leadTime.getProductId(), leadTime.getLocationId(), e.getMessage());
        }
    }

    private double calculateTotalLeadTimeDays(LeadTime leadTime) {
        double total = 0.0;
        if (leadTime.getOrderLeadTime() != null) {
            total += leadTime.getOrderLeadTime();
        }
        if (leadTime.getManufacturingTime() != null) {
            total += leadTime.getManufacturingTime();
        }
        if (leadTime.getTransportTime() != null) {
            total += leadTime.getTransportTime();
        }
        return total;
    }

    private String determineZone(int netAvailableQty, int bufferUnits) {
        if (bufferUnits == 0) return "GREEN";
        double inventoryPct = ((double) netAvailableQty / bufferUnits) * 100;
        if (inventoryPct >= 66.0) return "GREEN";
        else if (inventoryPct >= 33.0) return "YELLOW";
        else return "RED";
    }

    // Rest of the existing methods (getters, enrichment, etc.) remain unchanged...

    @Override
    @Transactional(readOnly = true)
    public LeadTimeResponseDTO getEnrichedLeadTimeById(String id) {
        log.info("Fetching enriched lead time: {}", id);
        LeadTime leadTime = leadTimeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead time not found: " + id));
        return enrichLeadTimeData(leadTime);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadTimeResponseDTO> getAllEnrichedLeadTimes(Pageable pageable) {
        log.info("Fetching all enriched lead times with pagination");
        Page<LeadTime> leadTimes = leadTimeRepository.findAll(pageable);
        return enrichLeadTimePage(leadTimes);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadTimeResponseDTO> getActiveEnrichedLeadTimes(Pageable pageable) {
        log.info("Fetching active enriched lead times with pagination");
        Page<LeadTime> leadTimes = leadTimeRepository.findByIsActive(true, pageable);
        return enrichLeadTimePage(leadTimes);
    }

    @Override
    @Transactional(readOnly = true)
    public LeadTimeResponseDTO getEnrichedActiveLeadTimeByProductAndLocation(String productId, String locationId) {
        log.info("Fetching enriched active lead time for product: {} at location: {}", productId, locationId);
        LeadTime leadTime = leadTimeRepository.findByProductIdAndLocationIdAndIsActive(productId, locationId, true)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Active lead time not found for product: " + productId + " at location: " + locationId));
        return enrichLeadTimeData(leadTime);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadTimeResponseDTO> getEnrichedLeadTimesByProduct(String productId, Pageable pageable) {
        log.info("Fetching enriched lead times by product: {}", productId);
        Page<LeadTime> leadTimes = leadTimeRepository.findByProductId(productId, pageable);
        return enrichLeadTimePage(leadTimes);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadTimeResponseDTO> getEnrichedLeadTimesByLocation(String locationId, Pageable pageable) {
        log.info("Fetching enriched lead times by location: {}", locationId);
        Page<LeadTime> leadTimes = leadTimeRepository.findByLocationId(locationId, pageable);
        return enrichLeadTimePage(leadTimes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeadTimeResponseDTO> getEnrichedLeadTimesByProductAndLocationIds(List<String> productIds, List<String> locationIds) {
        log.info("Fetching enriched lead times by product IDs: {} and location IDs: {}", productIds, locationIds);
        List<LeadTime> leadTimes = leadTimeRepository.findByProductIdInAndLocationIdInAndIsActive(productIds, locationIds, true);
        return enrichLeadTimeList(leadTimes);
    }

    // Helper methods for data enrichment
    private Page<LeadTimeResponseDTO> enrichLeadTimePage(Page<LeadTime> leadTimePage) {
        List<LeadTimeResponseDTO> enrichedList = enrichLeadTimeList(leadTimePage.getContent());
        return new PageImpl<>(enrichedList, leadTimePage.getPageable(), leadTimePage.getTotalElements());
    }

    private List<LeadTimeResponseDTO> enrichLeadTimeList(List<LeadTime> leadTimes) {
        if (leadTimes.isEmpty()) {
            return List.of();
        }

        // Collect all unique IDs for batch fetching
        List<String> productIds = leadTimes.stream()
                .map(LeadTime::getProductId)
                .distinct()
                .collect(Collectors.toList());
        List<String> supplierIds = leadTimes.stream()
                .map(LeadTime::getSupplierId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        List<String> locationIds = leadTimes.stream()
                .map(LeadTime::getLocationId)
                .distinct()
                .collect(Collectors.toList());

        // Batch fetch related data
        Map<String, Product> productMap = productRepository.findByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));

        Map<String, Supplier> supplierMap = supplierIds.isEmpty() ? Map.of() :
                supplierRepository.findBySupplierIdIn(supplierIds)
                        .stream()
                        .collect(Collectors.toMap(Supplier::getSupplierId, Function.identity()));

        Map<String, Location> locationMap = locationRepository.findByLocationIdIn(locationIds)
                .stream()
                .collect(Collectors.toMap(Location::getLocationId, Function.identity()));

        // Enrich each lead time
        return leadTimes.stream()
                .map(leadTime -> enrichLeadTimeData(leadTime, productMap, supplierMap, locationMap))
                .collect(Collectors.toList());
    }

    private LeadTimeResponseDTO enrichLeadTimeData(LeadTime leadTime) {
        Product product = productRepository.findByProductId(leadTime.getProductId()).orElse(null);
        Supplier supplier = leadTime.getSupplierId() != null ?
                supplierRepository.findById(leadTime.getSupplierId()).orElse(null) : null;
        Location location = locationRepository.findByLocationId(leadTime.getLocationId()).orElse(null);
        return buildEnrichedResponse(leadTime, product, supplier, location);
    }

    private LeadTimeResponseDTO enrichLeadTimeData(LeadTime leadTime,
                                                   Map<String, Product> productMap,
                                                   Map<String, Supplier> supplierMap,
                                                   Map<String, Location> locationMap) {
        Product product = productMap.get(leadTime.getProductId());
        Supplier supplier = leadTime.getSupplierId() != null ? supplierMap.get(leadTime.getSupplierId()) : null;
        Location location = locationMap.get(leadTime.getLocationId());
        return buildEnrichedResponse(leadTime, product, supplier, location);
    }

    private LeadTimeResponseDTO buildEnrichedResponse(LeadTime leadTime, Product product, Supplier supplier, Location location) {
        Double totalLeadTime = calculateTotalLeadTimeFromEntity(leadTime);
        return LeadTimeResponseDTO.builder()
                .id(leadTime.getId())
                .productId(leadTime.getProductId())
                .locationId(leadTime.getLocationId())
                .location(location != null ? location.getName() : null)
                .tenantSku(product != null ? product.getTenantSku() : null)
                .supplierSku(product != null ? product.getSupplierSku() : null)
                .supplierName(supplier != null ? supplier.getSupplierName() : null)
                .moq(leadTime.getMoq())
                .orderLeadTime(leadTime.getOrderLeadTime())
                .manufacturingTime(leadTime.getManufacturingTime())
                .transportTime(leadTime.getTransportTime())
                .totalLeadTimeDays(totalLeadTime)
                .isActive(leadTime.getIsActive())
                .updatedAt(leadTime.getUpdatedAt())
                .updatedBy(leadTime.getUpdatedBy())
                .build();
    }

    private Double calculateTotalLeadTimeFromEntity(LeadTime leadTime) {
        double total = 0.0;
        if (leadTime.getOrderLeadTime() != null) {
            total += leadTime.getOrderLeadTime();
        }
        if (leadTime.getManufacturingTime() != null) {
            total += leadTime.getManufacturingTime();
        }
        if (leadTime.getTransportTime() != null) {
            total += leadTime.getTransportTime();
        }
        return total;
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
                        "Active lead by time not found for product: " + productId + " at location: " + locationId));
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
        return calculateTotalLeadTimeFromEntity(leadTime);
    }

    // Mapping methods
    private LeadTime mapToEntity(LeadTimeDTO dto) {
        LeadTime leadTime = new LeadTime();
        leadTime.setProductId(dto.getProductId());
        leadTime.setLocationId(dto.getLocationId());
        leadTime.setSupplierId(dto.getSupplierId());
        leadTime.setOrderLeadTime(dto.getOrderLeadTime());
        leadTime.setManufacturingTime(dto.getManufacturingTime());
        leadTime.setTransportTime(dto.getTransportTime());
        leadTime.setMoq(dto.getMoq());
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
        dto.setMoq(entity.getMoq());
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
        entity.setMoq(dto.getMoq());
        entity.setUpdatedBy(dto.getUpdatedBy());
    }
}