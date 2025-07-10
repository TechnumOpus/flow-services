package com.onified.distribute.service.impl;

import com.onified.distribute.dto.*;
import com.onified.distribute.dto.InventoryOrderPipelineDTO;
import com.onified.distribute.dto.ReplenishmentQueueDTO;
import com.onified.distribute.entity.*;
import com.onified.distribute.exception.BadRequestException;
import com.onified.distribute.exception.ResourceNotFoundException;
import com.onified.distribute.repository.*;
import com.onified.distribute.service.InventoryBufferService;
import com.onified.distribute.service.InventoryOrderPipelineService;
import com.onified.distribute.service.ReplenishmentQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReplenishmentQueueServiceImpl implements ReplenishmentQueueService {

    private final ReplenishmentQueueRepository queueRepository;
    private final InventoryBufferRepository bufferRepository;
    private final ConsumptionProfileRepository consumptionProfileRepository;
    private final LeadTimeRepository leadTimeRepository;
    private final ProductRepository productRepository;
    private final InventoryOrderPipelineService inventoryOrderPipelineService;
    private final InventoryBufferService inventoryBufferService;
    private final LocationRepository locationRepository;
    private final InventoryOrderPipelineRepository inventoryOrderPipelineRepository;

    @Override
    public Page<ReplenishmentQueueResponseDTO> getAllReplenishmentQueuesEnhanced(Pageable pageable) {
        log.info("Fetching all replenishment queues with enhanced format");

        Page<ReplenishmentQueue> queues = queueRepository.findByIsActiveTrue(pageable);
        return queues.map(this::convertToEnhancedDto);
    }

    @Override
    public Page<ReplenishmentQueueResponseDTO> getReplenishmentQueuesWithFilters(
            ReplenishmentQueueFilterDTO filters, Pageable pageable) {
        log.info("Fetching replenishment queues with filters: {}", filters);

        Page<ReplenishmentQueue> queues = queueRepository.findByFilters(
                filters.getProductId(),
                filters.getLocationId(),
                filters.getBufferZone(),
                filters.getStatus(),
                filters.getMinBufferGap(),
                filters.getMaxBufferGap(),
                filters.getMinDaysOfSupply(),
                filters.getMaxDaysOfSupply(),
                filters.getRecommendedAction(),
                filters.getMinPriorityScore(),
                filters.getMaxPriorityScore(),
                pageable
        );

        return queues.map(this::convertToEnhancedDto);
    }

    private ReplenishmentQueueResponseDTO convertToEnhancedDto(ReplenishmentQueue queue) {
        ReplenishmentQueueResponseDTO dto = new ReplenishmentQueueResponseDTO();

        dto.setId(queue.getId());
        dto.setQueueId(queue.getQueueId());
        dto.setProductId(queue.getProductId());
        dto.setLocationId(queue.getLocationId());
        dto.setBufferUnits(queue.getBufferUnits());
        dto.setInHand(queue.getCurrentInventory());
        dto.setInPipeline(queue.getInPipelineQty());
        dto.setNetAvailability(queue.getNetAvailableQty());
        dto.setBufferGap(queue.getBufferGap());
        dto.setDaysOfSupply(queue.getDaysOfSupply());
        dto.setBufferZone(queue.getBufferZone());
        dto.setRecommendedAction(queue.getRecommendedAction());
        dto.setPriorityScore(queue.getPriorityScore());
        dto.setStatus(queue.getStatus());
        dto.setQueueDate(queue.getQueueDate());
        dto.setReasonCodes(queue.getReasonCodes());

        // Fetch product details for MOQ and name
        Optional<Product> product = productRepository.findByProductId(queue.getProductId());
        if (product.isPresent()) {
            dto.setMoq(product.get().getMoq());
            dto.setProductName(product.get().getName());
        } else {
            dto.setMoq(0);
            dto.setProductName("Unknown Product");
        }

        // Fetch location details for name
        Optional<Location> location = locationRepository.findByLocationId(queue.getLocationId());
        if (location.isPresent()) {
            dto.setLocationName(location.get().getName());
        } else {
            dto.setLocationName("Unknown Location");
        }

        return dto;
    }

    // Batch conversion method for better performance
    private List<ReplenishmentQueueResponseDTO> convertToEnhancedDtoBatch(List<ReplenishmentQueue> queues) {
        // Get unique product IDs and location IDs
        List<String> productIds = queues.stream()
                .map(ReplenishmentQueue::getProductId)
                .distinct()
                .collect(Collectors.toList());

        List<String> locationIds = queues.stream()
                .map(ReplenishmentQueue::getLocationId)
                .distinct()
                .collect(Collectors.toList());

        // Fetch products and locations in batch
        List<Product> products = productRepository.findByProductIdIn(productIds);
        List<Location> locations = locationRepository.findByLocationIdIn(locationIds);

        // Create maps for quick lookup
        Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p));

        Map<String, Location> locationMap = locations.stream()
                .collect(Collectors.toMap(Location::getLocationId, l -> l));

        // Convert to DTOs
        return queues.stream()
                .map(queue -> {
                    ReplenishmentQueueResponseDTO dto = new ReplenishmentQueueResponseDTO();

                    dto.setId(queue.getId());
                    dto.setQueueId(queue.getQueueId());
                    dto.setProductId(queue.getProductId());
                    dto.setLocationId(queue.getLocationId());
                    dto.setBufferUnits(queue.getBufferUnits());
                    dto.setInHand(queue.getCurrentInventory());
                    dto.setInPipeline(queue.getInPipelineQty());
                    dto.setNetAvailability(queue.getNetAvailableQty());
                    dto.setBufferGap(queue.getBufferGap());
                    dto.setDaysOfSupply(queue.getDaysOfSupply());
                    dto.setBufferZone(queue.getBufferZone());
                    dto.setRecommendedAction(queue.getRecommendedAction());
                    dto.setPriorityScore(queue.getPriorityScore());
                    dto.setStatus(queue.getStatus());
                    dto.setQueueDate(queue.getQueueDate());
                    dto.setReasonCodes(queue.getReasonCodes());

                    // Set product details
                    Product product = productMap.get(queue.getProductId());
                    if (product != null) {
                        dto.setMoq(product.getMoq());
                        dto.setProductName(product.getName());
                    } else {
                        dto.setMoq(0);
                        dto.setProductName("Unknown Product");
                    }

                    // Set location details
                    Location location = locationMap.get(queue.getLocationId());
                    if (location != null) {
                        dto.setLocationName(location.getName());
                    } else {
                        dto.setLocationName("Unknown Location");
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void processReplenishmentQueue() {
        log.info("Processing replenishment queue for pending items");

        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "priorityScore"));
        Page<ReplenishmentQueue> pendingItems = queueRepository.findByStatusAndIsActiveTrue("pending", pageable);

        for (ReplenishmentQueue queueItem : pendingItems.getContent()) {
            try {
                if (!"order".equalsIgnoreCase(queueItem.getRecommendedAction()) &&
                        !"expedite".equalsIgnoreCase(queueItem.getRecommendedAction())) {
                    log.debug("Skipping queue item {}: recommended action is {}",
                            queueItem.getQueueId(), queueItem.getRecommendedAction());
                    continue;
                }

                InventoryBuffer buffer = bufferRepository.findByProductIdAndLocationId(
                                queueItem.getProductId(), queueItem.getLocationId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Buffer not found for product: " + queueItem.getProductId() +
                                        ", location: " + queueItem.getLocationId()));

                Location location = locationRepository.findByLocationId(queueItem.getLocationId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Location not found: " + queueItem.getLocationId()));
                String supplierLocationId = location.getParentLocationId();
                if (supplierLocationId == null) {
                    throw new BadRequestException("Supplier not defined for location: " + queueItem.getLocationId());
                }

                InventoryOrderPipelineDTO orderDTO = new InventoryOrderPipelineDTO();
                String orderId = "ORD-" + UUID.randomUUID();
                orderDTO.setOrderId(orderId);
                orderDTO.setProductId(queueItem.getProductId());
                orderDTO.setLocationId(queueItem.getLocationId());
                orderDTO.setSupplierLocationId(supplierLocationId);
                orderDTO.setOrderType(queueItem.getRecommendedAction().equalsIgnoreCase("expedite") ? "EXPEDITE" : "STANDARD");
                orderDTO.setOrderedQty(calculateOrderedQty(queueItem, buffer));
                orderDTO.setReceivedQty(0);
                orderDTO.setPendingQty(orderDTO.getOrderedQty());
                orderDTO.setOrderDate(LocalDateTime.now());
                orderDTO.setExpectedReceiptDate(
                        queueItem.getLeadTimeDays() != null
                                ? LocalDateTime.now().plusDays(queueItem.getLeadTimeDays().longValue())
                                : LocalDateTime.now().plusDays(7));
                orderDTO.setStatus("DRAFT");
                orderDTO.setPriority(calculatePriority(queueItem, buffer));
                orderDTO.setCreatedAt(LocalDateTime.now());
                orderDTO.setUpdatedAt(LocalDateTime.now());
                orderDTO.setCreatedBy("SYSTEM");
                orderDTO.setLastStatusChange(LocalDateTime.now());
                orderDTO.setLastStatusChangeBy("SYSTEM");

                InventoryOrderPipelineDTO createdOrder = inventoryOrderPipelineService.createOrder(orderDTO);
                log.info("Created order {} for queue item {}", orderId, queueItem.getQueueId());

                Integer newInPipelineQty = buffer.getInPipelineQty() + orderDTO.getOrderedQty();
                InventoryBufferDTO bufferDTO = inventoryBufferService.getInventoryBufferById(buffer.getId());
                bufferDTO.setInPipelineQty(newInPipelineQty);
                inventoryBufferService.updateInventoryBuffer(buffer.getId(), bufferDTO);
                log.info("Updated in_pipeline_qty for buffer {}: {} -> {}",
                        buffer.getBufferId(), buffer.getInPipelineQty(), newInPipelineQty);

                queueItem.setStatus("processed");
                queueItem.setActionTaken(queueItem.getRecommendedAction());
                queueItem.setOrderId(orderId);
                queueItem.setProcessedAt(LocalDateTime.now());
                queueItem.setProcessedBy("SYSTEM");
                queueRepository.save(queueItem);
                log.info("Updated queue item {} to processed with order {}", queueItem.getQueueId(), orderId);

            } catch (Exception e) {
                log.error("Error processing queue item {}: {}", queueItem.getQueueId(), e.getMessage(), e);
            }
        }

        log.info("Replenishment queue processing completed. Processed {} items", pendingItems.getNumberOfElements());
    }

    @Override
    public void generateDailyReplenishmentQueue() {
        log.info("Starting hourly replenishment queue generation");

        try {
            validateRepositories();
            int pageSize = 1000;
            int pageNumber = 0;
            int processedCount = 0;
            int createdCount = 0;
            int errorCount = 0;

            while (true) {
                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                Page<InventoryBuffer> activeBuffersPage = bufferRepository.findActiveBuffers(pageable);

                if (activeBuffersPage.isEmpty()) {
                    log.info("No more active buffers to process. Breaking pagination loop.");
                    break;
                }

                log.debug("Processing page {} with {} buffers", pageNumber, activeBuffersPage.getNumberOfElements());

                for (InventoryBuffer buffer : activeBuffersPage.getContent()) {
                    try {
                        if (buffer == null || buffer.getProductId() == null || buffer.getLocationId() == null) {
                            log.warn("Buffer {} has null productId or locationId, skipping", buffer != null ? buffer.getBufferId() : "null");
                            continue;
                        }

                        ReplenishmentQueue queueItem = processBuffer(buffer);
                        if (queueItem != null) {
                            queueRepository.save(queueItem);
                            createdCount++;
                            log.debug("Created queue item for buffer: {}", buffer.getBufferId());
                        } else {
                            log.debug("No queue item created for buffer: {} (likely missing dependencies)", buffer.getBufferId());
                        }
                        processedCount++;
                    } catch (Exception bufferException) {
                        log.error("Error processing buffer: {} - {}", buffer != null ? buffer.getBufferId() : "null", bufferException.getMessage(), bufferException);
                        errorCount++;
                        processedCount++;
                    }
                }

                if (!activeBuffersPage.hasNext()) {
                    break;
                }
                pageNumber++;
            }

            log.info("Hourly replenishment queue generation completed. Processed: {}, Created: {}, Errors: {}", processedCount, createdCount, errorCount);

        } catch (Exception e) {
            log.error("Critical error during hourly replenishment queue generation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate hourly replenishment queue: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<ReplenishmentQueueDTO> getAllReplenishmentQueues(Pageable pageable) {
        log.info("Fetching all replenishment queues");
        return queueRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    public ReplenishmentQueueDTO createQueueItem(ReplenishmentQueueDTO queueDTO) {
        log.info("Creating replenishment queue item with ID: {}", queueDTO.getQueueId());
        if (queueRepository.findByQueueId(queueDTO.getQueueId()) != null) {
            throw new BadRequestException("Queue ID already exists: " + queueDTO.getQueueId());
        }
        ReplenishmentQueue queueItem = mapToEntity(queueDTO);
        queueItem.setQueueId(UUID.randomUUID().toString());
        queueItem.setQueueDate(LocalDateTime.now());
        queueItem.setIsActive(true);
        ReplenishmentQueue savedQueueItem = queueRepository.save(queueItem);
        return convertToDto(savedQueueItem);
    }

    @Override
    public ReplenishmentQueueDTO updateQueueItem(String queueId, ReplenishmentQueueDTO queueDTO) {
        log.info("Updating replenishment queue item: {}", queueId);
        ReplenishmentQueue existingQueueItem = queueRepository.findByQueueId(queueId);
        if (existingQueueItem == null) {
            throw new ResourceNotFoundException("Queue item not found with ID: " + queueId);
        }
        updateEntityFromDto(queueDTO, existingQueueItem);
        existingQueueItem.setProcessedAt(LocalDateTime.now());
        ReplenishmentQueue savedQueueItem = queueRepository.save(existingQueueItem);
        return convertToDto(savedQueueItem);
    }

    @Override
    @Transactional(readOnly = true)
    public ReplenishmentQueueDTO getQueueItemById(String queueId) {
        log.info("Fetching replenishment queue item: {}", queueId);
        ReplenishmentQueue queueItem = queueRepository.findByQueueId(queueId);
        if (queueItem == null) {
            throw new ResourceNotFoundException("Queue item not found with ID: " + queueId);
        }
        return convertToDto(queueItem);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReplenishmentQueueDTO> getQueueItemsByStatus(String status, Pageable pageable) {
        log.info("Fetching queue items by status: {}", status);
        return queueRepository.findByStatusAndIsActiveTrue(status, pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReplenishmentQueueDTO> getQueueItemsByBufferZoneAndStatus(String bufferZone, String status, Pageable pageable) {
        log.info("Fetching queue items by buffer zone: {} and status: {}", bufferZone, status);
        return queueRepository.findByBufferZoneAndStatusAndIsActiveTrue(bufferZone, status, pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryOrderPipelineDTO> getInTransitOrders(String locationId, Pageable pageable) {
        log.info("Fetching in-transit orders for location: {}", locationId);
        List<String> inTransitStatuses = Arrays.asList("CONFIRMED", "SHIPPED", "IN_TRANSIT");
        return inventoryOrderPipelineRepository.findByStatusInAndLocationId(inTransitStatuses, locationId, pageable)
                .map(order -> {
                    InventoryOrderPipelineDTO dto = new InventoryOrderPipelineDTO();
                    dto.setId(order.getId());
                    dto.setOrderId(order.getOrderId());
                    dto.setProductId(order.getProductId());
                    dto.setLocationId(order.getLocationId());
                    dto.setSupplierLocationId(order.getSupplierLocationId());
                    dto.setOrderType(order.getOrderType());
                    dto.setOrderedQty(order.getOrderedQty());
                    dto.setReceivedQty(order.getReceivedQty());
                    dto.setPendingQty(order.getPendingQty());
                    dto.setOrderDate(order.getOrderDate());
                    dto.setExpectedReceiptDate(order.getExpectedReceiptDate());
                    dto.setActualReceiptDate(order.getActualReceiptDate());
                    dto.setStatus(order.getStatus());
                    dto.setPriority(order.getPriority());
                    return dto;
                });
    }

    @Override
    public void cancelOrder(String orderId) {
        log.info("Canceling order: {}", orderId);
        InventoryOrderPipeline order = inventoryOrderPipelineRepository.findByOrderId(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found with ID: " + orderId);
        }

        InventoryBuffer buffer = bufferRepository.findByProductIdAndLocationId(
                        order.getProductId(), order.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Buffer not found for product: " + order.getProductId() + ", location: " + order.getLocationId()));
        Integer newInPipelineQty = Math.max(0, buffer.getInPipelineQty() - order.getOrderedQty());
        InventoryBufferDTO bufferDTO = inventoryBufferService.getInventoryBufferById(buffer.getId());
        bufferDTO.setInPipelineQty(newInPipelineQty);
        inventoryBufferService.updateInventoryBuffer(buffer.getId(), bufferDTO);
        log.info("Updated in_pipeline_qty for buffer {}: {} -> {}", buffer.getBufferId(), buffer.getInPipelineQty(), newInPipelineQty);

        ReplenishmentQueue queueItem = queueRepository.findByOrderId(orderId);
        if (queueItem != null) {
            queueItem.setStatus("canceled");
            queueItem.setActionTaken("canceled");
            queueItem.setProcessedAt(LocalDateTime.now());
            queueItem.setProcessedBy("SYSTEM");
            queueRepository.save(queueItem);
            log.info("Updated queue item {} to canceled for order {}", queueItem.getQueueId(), orderId);
        }

        inventoryOrderPipelineRepository.delete(order);
        log.info("Deleted order {} from inventory_orders_pipeline", orderId);
    }

    private ReplenishmentQueueDTO convertToDto(ReplenishmentQueue queueItem) {
        ReplenishmentQueueDTO dto = new ReplenishmentQueueDTO();
        dto.setId(queueItem.getId());
        dto.setQueueId(queueItem.getQueueId());
        dto.setProductId(queueItem.getProductId());
        dto.setLocationId(queueItem.getLocationId());
        dto.setCurrentInventory(queueItem.getCurrentInventory());
        dto.setInPipelineQty(queueItem.getInPipelineQty());
        dto.setAllocatedQty(queueItem.getAllocatedQty());
        dto.setNetAvailableQty(queueItem.getNetAvailableQty());
        dto.setBufferUnits(queueItem.getBufferUnits());
        dto.setBufferGap(queueItem.getBufferGap());
        dto.setBufferZone(queueItem.getBufferZone());
        dto.setDaysOfSupply(queueItem.getDaysOfSupply());
        // Note: finalQuantity is not included as per requirements
        dto.setRecommendedQty(queueItem.getRecommendedQty());
        dto.setRecommendedAction(queueItem.getRecommendedAction());
        dto.setPriorityScore(queueItem.getPriorityScore());
        dto.setAdcUsed(queueItem.getAdcUsed());
        dto.setLeadTimeDays(queueItem.getLeadTimeDays());
        dto.setReasonCodes(queueItem.getReasonCodes());
        dto.setQueueDate(queueItem.getQueueDate());
        dto.setProcessedBy(queueItem.getProcessedBy());
        dto.setProcessedAt(queueItem.getProcessedAt());
        dto.setActionTaken(queueItem.getActionTaken());
        dto.setOrderId(queueItem.getOrderId());
        dto.setStatus(queueItem.getStatus());
        dto.setIsActive(queueItem.getIsActive());
        return dto;
    }

    private ReplenishmentQueue mapToEntity(ReplenishmentQueueDTO dto) {
        ReplenishmentQueue queueItem = new ReplenishmentQueue();
        queueItem.setQueueId(dto.getQueueId());
        queueItem.setProductId(dto.getProductId());
        queueItem.setLocationId(dto.getLocationId());
        queueItem.setCurrentInventory(dto.getCurrentInventory());
        queueItem.setInPipelineQty(dto.getInPipelineQty());
        queueItem.setAllocatedQty(dto.getAllocatedQty());
        queueItem.setNetAvailableQty(dto.getNetAvailableQty());
        queueItem.setBufferUnits(dto.getBufferUnits());
        queueItem.setBufferGap(dto.getBufferGap());
        queueItem.setBufferZone(dto.getBufferZone());
        queueItem.setDaysOfSupply(dto.getDaysOfSupply());
        queueItem.setRecommendedQty(dto.getRecommendedQty());
        queueItem.setRecommendedAction(dto.getRecommendedAction());
        queueItem.setPriorityScore(dto.getPriorityScore());
        queueItem.setAdcUsed(dto.getAdcUsed());
        queueItem.setLeadTimeDays(dto.getLeadTimeDays());
        queueItem.setReasonCodes(dto.getReasonCodes());
        queueItem.setQueueDate(dto.getQueueDate());
        queueItem.setProcessedBy(dto.getProcessedBy());
        queueItem.setProcessedAt(dto.getProcessedAt());
        queueItem.setActionTaken(dto.getActionTaken());
        queueItem.setOrderId(dto.getOrderId());
        queueItem.setStatus(dto.getStatus());
        queueItem.setIsActive(dto.getIsActive());
        return queueItem;
    }

    private void updateEntityFromDto(ReplenishmentQueueDTO dto, ReplenishmentQueue queueItem) {
        queueItem.setProductId(dto.getProductId());
        queueItem.setLocationId(dto.getLocationId());
        queueItem.setCurrentInventory(dto.getCurrentInventory());
        queueItem.setInPipelineQty(dto.getInPipelineQty());
        queueItem.setAllocatedQty(dto.getAllocatedQty());
        queueItem.setNetAvailableQty(dto.getNetAvailableQty());
        queueItem.setBufferUnits(dto.getBufferUnits());
        queueItem.setBufferGap(dto.getBufferGap());
        queueItem.setBufferZone(dto.getBufferZone());
        queueItem.setDaysOfSupply(dto.getDaysOfSupply());
        queueItem.setRecommendedQty(dto.getRecommendedQty());
        queueItem.setRecommendedAction(dto.getRecommendedAction());
        queueItem.setPriorityScore(dto.getPriorityScore());
        queueItem.setAdcUsed(dto.getAdcUsed());
        queueItem.setLeadTimeDays(dto.getLeadTimeDays());
        queueItem.setReasonCodes(dto.getReasonCodes());
        queueItem.setProcessedBy(dto.getProcessedBy());
        queueItem.setActionTaken(dto.getActionTaken());
        queueItem.setOrderId(dto.getOrderId());
        queueItem.setStatus(dto.getStatus());
        queueItem.setIsActive(dto.getIsActive());
    }

    private Integer calculateOrderedQty(ReplenishmentQueue queueItem, InventoryBuffer buffer) {
        Integer bufferUnits = buffer.getBufferUnits() != null ? buffer.getBufferUnits() : 0;
        Integer currentInventory = buffer.getCurrentInventory() != null ? buffer.getCurrentInventory() : 0;
        Integer orderedQty = Math.max(0, bufferUnits - currentInventory);
        log.debug("Calculated ordered_qty for queue item {}: {} (bufferUnits={} - currentInventory={})",
                queueItem.getQueueId(), orderedQty, bufferUnits, currentInventory);
        return orderedQty;
    }

    private String calculatePriority(ReplenishmentQueue queueItem, InventoryBuffer buffer) {
        double zoneWeight = switch (buffer.getCurrentZone().toLowerCase()) {
            case "red", "critical" -> 100.0;
            case "yellow" -> 50.0;
            case "green" -> 10.0;
            default -> 10.0;
        };
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long orderCount = inventoryOrderPipelineRepository
                .findByLocationId(queueItem.getLocationId(), Pageable.unpaged())
                .getContent().stream()
                .filter(order -> order.getOrderDate() != null && order.getOrderDate().isAfter(thirtyDaysAgo))
                .count();
        double frequencyWeight = orderCount * 5.0;
        double priorityScore = zoneWeight + frequencyWeight;
        String priority = switch ((int) (priorityScore / 50)) {
            case 0 -> "LOW";
            case 1 -> "MEDIUM";
            case 2 -> "HIGH";
            default -> "CRITICAL";
        };
        log.debug("Calculated priority for queue item {}: zoneWeight={}, frequencyWeight={}, priority={}",
                queueItem.getQueueId(), zoneWeight, frequencyWeight, priority);
        return priority;
    }

    private ReplenishmentQueue processBuffer(InventoryBuffer buffer) {
        log.debug("Processing buffer: {} for Product: {}, Location: {}", buffer.getBufferId(), buffer.getProductId(), buffer.getLocationId());

        try {
            if (buffer.getProductId() == null || buffer.getLocationId() == null) {
                log.warn("Buffer {} has null productId or locationId", buffer.getBufferId());
                return null;
            }

            ConsumptionProfile consumptionProfile = consumptionProfileRepository
                    .findByProductIdAndLocationId(buffer.getProductId(), buffer.getLocationId())
                    .orElse(null);
            LeadTime leadTime = leadTimeRepository
                    .findByProductIdAndLocationIdAndIsActive(buffer.getProductId(), buffer.getLocationId(), true)
                    .orElse(null);
            Product product = productRepository
                    .findByProductId(buffer.getProductId())
                    .orElse(null);

            if (consumptionProfile == null || leadTime == null || product == null) {
                log.debug("Missing data for buffer: {} (Product: {}, Location: {}) - CP: {}, LT: {}, P: {}",
                        buffer.getBufferId(), buffer.getProductId(), buffer.getLocationId(),
                        consumptionProfile != null, leadTime != null, product != null);
                return null;
            }

            Integer currentInventory = buffer.getCurrentInventory() != null ? buffer.getCurrentInventory() : 0;
            Integer inPipelineQty = buffer.getInPipelineQty() != null ? buffer.getInPipelineQty() : 0;
            Integer netAvailableQty = buffer.getNetAvailableQty() != null ? buffer.getNetAvailableQty() : 0;
            Integer bufferUnits = buffer.getBufferUnits() != null ? buffer.getBufferUnits() : 0;
            String currentZone = buffer.getCurrentZone() != null ? buffer.getCurrentZone() : "GREEN";

            Integer bufferDeficit = calculateBufferDeficit(buffer);
            Double daysOfSupply = calculateDaysOfSupply(buffer, consumptionProfile);
            String recommendedAction = determineRecommendedAction(buffer, daysOfSupply, leadTime);
            Integer recommendedQty = calculateRecommendedQty(bufferDeficit, product.getMoq(), recommendedAction);
            Double priorityScore = calculatePriorityScore(buffer, bufferDeficit);
            List<String> reasonCodes = generateReasonCodes(buffer, daysOfSupply, leadTime);

            ReplenishmentQueue queueItem = new ReplenishmentQueue();
            queueItem.setQueueId(generateQueueId(buffer));
            queueItem.setProductId(buffer.getProductId());
            queueItem.setLocationId(buffer.getLocationId());
            queueItem.setCurrentInventory(currentInventory);
            queueItem.setInPipelineQty(inPipelineQty);
            queueItem.setNetAvailableQty(netAvailableQty);
            queueItem.setBufferUnits(bufferUnits);
            queueItem.setBufferGap(bufferDeficit);
            queueItem.setBufferZone(currentZone);
            queueItem.setDaysOfSupply(daysOfSupply);
            queueItem.setRecommendedQty(recommendedQty);
            queueItem.setRecommendedAction(recommendedAction);
            queueItem.setPriorityScore(priorityScore);
            queueItem.setAdcUsed(consumptionProfile.getAdcNormalized());
            queueItem.setReasonCodes(reasonCodes);
            queueItem.setQueueDate(LocalDateTime.now());
            queueItem.setStatus("PENDING");
            queueItem.setIsActive(true);

            log.debug("Successfully created queue item for buffer: {}", buffer.getBufferId());
            return queueItem;

        } catch (Exception e) {
            log.error("Error in processBuffer for buffer: {} - {}", buffer.getBufferId(), e.getMessage(), e);
            return null;
        }
    }

    private Integer calculateBufferDeficit(InventoryBuffer buffer) {
        Integer bufferUnits = buffer.getBufferUnits() != null ? buffer.getBufferUnits() : 0;
        Integer netAvailable = buffer.getNetAvailableQty() != null ? buffer.getNetAvailableQty() : 0;
        return Math.max(0, bufferUnits - netAvailable);
    }

    private Double calculateDaysOfSupply(InventoryBuffer buffer, ConsumptionProfile consumptionProfile) {
        Double adcNormalized = consumptionProfile.getAdcNormalized();
        if (adcNormalized == null || adcNormalized <= 0) {
            return 0.0;
        }
        Integer netAvailable = buffer.getNetAvailableQty() != null ? buffer.getNetAvailableQty() : 0;
        return netAvailable.doubleValue() / adcNormalized;
    }

    private String determineRecommendedAction(InventoryBuffer buffer, Double daysOfSupply, LeadTime leadTime) {
        Integer bufferDeficit = calculateBufferDeficit(buffer);
        String currentZone = buffer.getCurrentZone() != null ? buffer.getCurrentZone() : "GREEN";
        Double replenishmentLeadTimeDays = leadTime.getManufacturingTime() + leadTime.getOrderLeadTime() + leadTime.getTransportTime();
        Double leadTimeDays = replenishmentLeadTimeDays != null ? replenishmentLeadTimeDays : 7.0;

        if (bufferDeficit > 0) {
            if ("RED".equalsIgnoreCase(currentZone) && daysOfSupply < (leadTimeDays / 2.0)) {
                return "EXPEDITE";
            }
            return "ORDER";
        }
        return "MONITOR";
    }

    private Integer calculateRecommendedQty(Integer bufferDeficit, Integer moq, String recommendedAction) {
        if ("MONITOR".equals(recommendedAction)) {
            return 0;
        }
        if (bufferDeficit <= 0) {
            return 0;
        }
        Integer minOrderQty = moq != null ? moq : 1;
        return Math.max(minOrderQty, bufferDeficit);
    }

    private Double calculatePriorityScore(InventoryBuffer buffer, Integer bufferDeficit) {
        String currentZone = buffer.getCurrentZone() != null ? buffer.getCurrentZone().toLowerCase() : "GREEN";
        double zoneWeight = switch (currentZone) {
            case "red", "critical" -> 100.0;
            case "yellow" -> 50.0;
            case "green" -> 10.0;
            default -> 10.0;
        };
        double bufferPenetration = 0.0;
        Integer bufferUnits = buffer.getBufferUnits();
        if (bufferUnits != null && bufferUnits > 0 && bufferDeficit != null) {
            bufferPenetration = (bufferDeficit.doubleValue() / bufferUnits) * 100;
        }
        return zoneWeight + bufferPenetration;
    }

    private List<String> generateReasonCodes(InventoryBuffer buffer, Double daysOfSupply, LeadTime leadTime) {
        List<String> reasonCodes = new ArrayList<>();
        String currentZone = buffer.getCurrentZone() != null ? buffer.getCurrentZone().toLowerCase() : "GREEN";
        Double replenishmentLeadTimeDays = leadTime.getManufacturingTime() + leadTime.getOrderLeadTime() + leadTime.getTransportTime();
        Double leadTimeDays = replenishmentLeadTimeDays != null ? replenishmentLeadTimeDays : 7.0;

        if ("red".equalsIgnoreCase(currentZone)) {
            reasonCodes.add("RED_ZONE");
        } else if ("yellow".equalsIgnoreCase(currentZone)) {
            reasonCodes.add("YELLOW_ZONE");
        }
        if (daysOfSupply != null && daysOfSupply < leadTimeDays) {
            reasonCodes.add("LOW_DAYS_OF_SUPPLY");
        }
        Integer bufferDeficit = calculateBufferDeficit(buffer);
        if (bufferDeficit > 0) {
            reasonCodes.add("BUFFER_DEFICIT");
        }
        if (reasonCodes.isEmpty()) {
            reasonCodes.add("ROUTINE_REVIEW");
        }
        return reasonCodes;
    }

    private String generateQueueId(InventoryBuffer buffer) {
        String bufferId = buffer.getBufferId() != null ? buffer.getBufferId() : "UNKNOWN";
        return String.format("QUEUE-%d-%s", System.currentTimeMillis(), bufferId);
    }

    private void validateRepositories() {
        if (bufferRepository == null) {
            throw new IllegalStateException("BufferRepository is not initialized");
        }
        if (queueRepository == null) {
            throw new IllegalStateException("QueueRepository is not initialized");
        }
        if (consumptionProfileRepository == null) {
            throw new IllegalStateException("ConsumptionProfileRepository is not initialized");
        }
        if (leadTimeRepository == null) {
            throw new IllegalStateException("LeadTimeRepository is not initialized");
        }
        if (productRepository == null) {
            throw new IllegalStateException("ProductRepository is not initialized");
        }
    }
}
