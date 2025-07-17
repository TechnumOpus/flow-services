package com.onified.distribute.service.impl.order;

import com.onified.distribute.dto.*;
import com.onified.distribute.dto.request.CreateOrdersRequestDTO;
import com.onified.distribute.entity.*;
import com.onified.distribute.exception.BadRequestException;
import com.onified.distribute.exception.ResourceNotFoundException;
import com.onified.distribute.repository.*;
import com.onified.distribute.service.dbm.InventoryBufferService;
import com.onified.distribute.service.order.InventoryOrderPipelineService;
import com.onified.distribute.service.order.ReplenishmentOverrideLogService;
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
public class InventoryOrderPipelineServiceImpl implements InventoryOrderPipelineService {

    private final InventoryOrderPipelineRepository orderRepository;
    private final ReplenishmentQueueRepository queueRepository;
    private final InventoryBufferRepository bufferRepository;
    private final InventoryBufferService inventoryBufferService;
    private final LocationRepository locationRepository;
    private final ReplenishmentOverrideLogService overrideLogService;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryOrderPipelineDTO> getPendingOrders(String locationId, Pageable pageable) {
        log.info("Retrieving pending orders with locationId filter: {}", locationId);
        List<InventoryOrderPipeline> allOrders = orderRepository.findAll(pageable).getContent();
        Map<String, InventoryOrderPipeline> uniqueOrders = new HashMap<>();

        for (InventoryOrderPipeline order : allOrders) {
            String key = order.getProductId() + "|" + order.getLocationId();
            if ("DRAFT".equals(order.getStatus())) {
                uniqueOrders.put(key, order); // Keep DRAFT
            } else if ("FAILED".equals(order.getStatus()) && !uniqueOrders.containsKey(key)) {
                uniqueOrders.put(key, order); // Keep FAILED only if no DRAFT exists
            }
        }

        List<InventoryOrderPipeline> filteredOrders = new ArrayList<>(uniqueOrders.values());
        if (locationId != null) {
            filteredOrders.removeIf(order -> !locationId.equals(order.getLocationId()));
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredOrders.size());
        List<InventoryOrderPipeline> pagedOrders = filteredOrders.subList(start, end);

        return new PageImpl<>(pagedOrders.stream().map(this::convertToDto).collect(Collectors.toList()),
                pageable, filteredOrders.size());
    }

    @Override
    @Transactional
    public void approveSelectedOrders(List<String> orderIds, String userId) {
        log.info("Approving selected orders: {}", orderIds);
        String effectiveUserId = userId != null ? userId : "SYSTEM";
        LocalDateTime now = LocalDateTime.now();

        for (String orderId : orderIds) {
            InventoryOrderPipeline order = orderRepository.findByOrderId(orderId);
            if (order == null) {
                log.warn("Order not found: {}", orderId);
                continue;
            }
            if (!"DRAFT".equals(order.getStatus())) {
                log.warn("Order {} is not in DRAFT status, skipping", orderId);
                continue;
            }

            order.setStatus("PROCESSED");
           orderRepository.save(order);
            log.info("Approved order: {}", orderId);

            // Optionally update related queue item if linked
            ReplenishmentQueue queue = queueRepository.findByOrderId(orderId);
            if (queue != null) {
                queue.setStatus("PROCESSED");
                queue.setActionTaken("APPROVED");
                queue.setProcessedAt(now);
                queue.setProcessedBy(effectiveUserId);
                queueRepository.save(queue);
                log.info("Updated queue item for order {} to PROCESSED", orderId);
            }
        }
    }

    @Override
    @Transactional
    public void approveAllPendingOrders(String userId) {
        log.info("Approving all pending orders");
        List<InventoryOrderPipeline> draftOrders = orderRepository.findByStatusInAndLocationId(
                Arrays.asList("DRAFT"), null, Pageable.unpaged()).getContent();
        String effectiveUserId = userId != null ? userId : "SYSTEM";

        for (InventoryOrderPipeline order : draftOrders) {
            order.setStatus("PROCESSED");
     orderRepository.save(order);
            log.info("Approved order: {}", order.getOrderId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryOrderPipelineDTO> getActiveOrders(String status, String locationId, Pageable pageable) {
        log.info("Retrieving active orders with status filter: {}, locationId: {}", status, locationId);

        Page<InventoryOrderPipeline> orders;

        if (status != null && !status.isEmpty()) {
            // If specific status is provided, filter by that status (excluding DRAFT)
            if ("DRAFT".equalsIgnoreCase(status)) {
                // If someone specifically asks for DRAFT, return empty page or handle as needed
                return new PageImpl<>(Collections.emptyList(), pageable, 0);
            }
            // Filter by specific status and location
            if (locationId != null && !locationId.isEmpty()) {
                orders = orderRepository.findByStatusAndLocationId(status, locationId, pageable);
            } else {
                orders = orderRepository.findByStatusNot(status, pageable);
            }
        } else {
            // If no specific status, get all except DRAFT
            if (locationId != null && !locationId.isEmpty()) {
                orders = orderRepository.findByStatusNotAndLocationId("DRAFT", locationId, pageable);
            } else {
                orders = orderRepository.findByStatusNot("DRAFT", pageable);
            }
        }

        return orders.map(this::convertToDto);
    }
    @Override
    public InventoryOrderPipelineDTO createOrder(InventoryOrderPipelineDTO orderDTO) {
        log.info("Creating inventory order pipeline");

        // Generate orderId if not provided
        if (orderDTO.getOrderId() == null || orderDTO.getOrderId().isEmpty()) {
            String randomCode = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            orderDTO.setOrderId("ORD-" + randomCode);
        }

        // Check if orderId already exists
        if (orderRepository.findByOrderId(orderDTO.getOrderId()) != null) {
            throw new BadRequestException("Order ID already exists: " + orderDTO.getOrderId());
        }

        InventoryOrderPipeline order = mapToEntity(orderDTO);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        InventoryOrderPipeline savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }

    @Override
    public void createBulkOrders(CreateOrdersRequestDTO requestDTO, String userId) {
        log.info("Creating bulk orders for {} queue items", requestDTO.getQueueItems().size());

        String effectiveUserId = userId != null ? userId : "SYSTEM";
        for (CreateOrdersRequestDTO.QueueItem queueItem : requestDTO.getQueueItems()) {
            try {
                // Validate queue item
                ReplenishmentQueue queue = queueRepository.findByQueueId(queueItem.getQueueId());
                if (queue == null) {
                    throw new ResourceNotFoundException("Queue item not found with ID: " + queueItem.getQueueId());
                }

                // Validate buffer
                InventoryBuffer buffer = bufferRepository.findByProductIdAndLocationId(
                                queueItem.getProductId(), queueItem.getLocationId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Buffer not found for product: " + queueItem.getProductId() +
                                        ", location: " + queueItem.getLocationId()));

                // Validate location and supplier
                Location location = locationRepository.findByLocationId(queueItem.getLocationId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Location not found: " + queueItem.getLocationId()));

                Optional<Product> product = productRepository.findByProductId(queueItem.getProductId());

                String supplierLocationId = product.get().getSupplierName();

                if (supplierLocationId == null) {
                    throw new BadRequestException("Supplier not defined for Products: " + product.get().getSupplierName());
                }

                // Log override if finalQuantity differs from recommendedQty
                if (queueItem.getFinalQuantity() != null && !queueItem.getFinalQuantity().equals(queue.getRecommendedQty())) {
                    ReplenishmentOverrideLogDTO overrideLogDTO = new ReplenishmentOverrideLogDTO();
                    overrideLogDTO.setProductId(queueItem.getProductId());
                    overrideLogDTO.setLocationId(queueItem.getLocationId());
                    overrideLogDTO.setOriginalQuantity(queue.getRecommendedQty());
                    overrideLogDTO.setOverriddenQuantity(queueItem.getFinalQuantity());
                    overrideLogDTO.setReason(queueItem.getOverrideReason() != null ? queueItem.getOverrideReason() : "Manual override by user");
                    overrideLogDTO.setApprover(queueItem.getApprover() != null ? queueItem.getApprover() : effectiveUserId);
                    overrideLogDTO.setCreatedBy(effectiveUserId);
                    overrideLogDTO.setTimestamp(LocalDateTime.now());
                    overrideLogService.createOverrideLog(overrideLogDTO);
                    log.info("Logged override for queue item {}: originalQty={}, overriddenQty={}",
                            queueItem.getQueueId(), queue.getRecommendedQty(), queueItem.getFinalQuantity());
                }

                ReplenishmentQueueResponseDTO dto = new ReplenishmentQueueResponseDTO();
                InventoryOrderPipelineDTO orderDTO = new InventoryOrderPipelineDTO();
                String orderId = "ORD-" + UUID.randomUUID();
                orderDTO.setOrderId(orderId);
                orderDTO.setProductId(queueItem.getProductId());
                orderDTO.setLocationId(queueItem.getLocationId());
                orderDTO.setOrderType(queue.getRecommendedAction().equalsIgnoreCase("EXPEDITE") ? "EXPEDITE" : "STANDARD");
                orderDTO.setOrderedQty(queueItem.getFinalQuantity() != null ? queueItem.getFinalQuantity() : queue.getRecommendedQty());


                orderDTO.setStatus("DRAFT");
                orderDTO.setCreatedAt(LocalDateTime.now());
                orderDTO.setUpdatedAt(LocalDateTime.now());
                orderDTO.setCreatedBy(effectiveUserId);


                InventoryOrderPipelineDTO createdOrder = createOrder(orderDTO);
                log.info("Created order {} for queue item {}", orderId, queueItem.getQueueId());

                // Update buffer in_pipeline_qty
                Integer newInPipelineQty = buffer.getInPipelineQty() + orderDTO.getOrderedQty();
                InventoryBufferDTO bufferDTO = inventoryBufferService.getInventoryBufferById(buffer.getId());
                bufferDTO.setInPipelineQty(newInPipelineQty);
                inventoryBufferService.updateInventoryBuffer(buffer.getId(), bufferDTO);
                log.info("Updated in_pipeline_qty for buffer {}: {} -> {}", buffer.getBufferId(), buffer.getInPipelineQty(), newInPipelineQty);

                // Update queue item
                queue.setStatus("PROCESSED");
                queue.setActionTaken(queue.getRecommendedAction());
                queue.setOrderId(orderId);
                queue.setProcessedAt(LocalDateTime.now());
                queue.setProcessedBy(effectiveUserId);
                queueRepository.save(queue);
                log.info("Updated queue item {} to processed with order {}", queue.getQueueId(), orderId);

            } catch (Exception e) {
                log.error("Error processing queue item {}: {}", queueItem.getQueueId(), e.getMessage(), e);
            }
        }
    }

    @Override
    public InventoryOrderPipelineDTO updateOrder(String orderId, InventoryOrderPipelineDTO orderDTO) {
        log.info("Updating inventory order pipeline: {}", orderId);

        InventoryOrderPipeline existingOrder = orderRepository.findByOrderId(orderId);
        if (existingOrder == null) {
            throw new ResourceNotFoundException("Order not found with ID: " + orderId);
        }

        updateEntityFromDto(orderDTO, existingOrder);
        existingOrder.setUpdatedAt(LocalDateTime.now());

        InventoryOrderPipeline savedOrder = orderRepository.save(existingOrder);
        return convertToDto(savedOrder);
    }

    @Override
    public InventoryOrderPipelineDTO updateOrderReceipt(String orderId, Integer receivedQty, LocalDateTime actualReceiptDate) {
        log.info("Updating order receipt for order: {}", orderId);

        InventoryOrderPipeline order = orderRepository.findByOrderId(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found with ID: " + orderId);
        }


        if (actualReceiptDate != null) {
            order.setActualReceiptDate(actualReceiptDate);
        }
        order.setStatus("RECEIVED");
        order.setUpdatedAt(LocalDateTime.now());

        InventoryOrderPipeline savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryOrderPipelineDTO getOrderById(String orderId) {
        log.info("Fetching inventory order pipeline: {}", orderId);

        InventoryOrderPipeline order = orderRepository.findByOrderId(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found with ID: " + orderId);
        }

        return convertToDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryOrderPipelineDTO> getOrdersByStatusAndLocation(String status, String locationId, Pageable pageable) {
        log.info("Fetching orders by status: {} and location: {}", status, locationId);
        return orderRepository.findByStatusAndLocationId(status, locationId, pageable).map(this::convertToDto);
    }

    @Override
    public void deleteOrder(String orderId) {
        log.info("Deleting inventory order pipeline: {}", orderId);

        InventoryOrderPipeline order = orderRepository.findByOrderId(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found with ID: " + orderId);
        }

        orderRepository.delete(order);
        log.info("Deleted order {} from inventory_orders_pipeline", orderId);
    }

    private InventoryOrderPipelineDTO convertToDto(InventoryOrderPipeline order) {
        InventoryOrderPipelineDTO dto = new InventoryOrderPipelineDTO();
        dto.setOrderId(order.getOrderId());
        dto.setProductId(order.getProductId());
        dto.setLocationId(order.getLocationId());
        dto.setOrderType(order.getOrderType());
        dto.setOrderedQty(order.getOrderedQty());


        dto.setSupplierName(order.getSupplierName());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setCreatedBy(order.getCreatedBy());

        return dto;
    }

    private InventoryOrderPipeline mapToEntity(InventoryOrderPipelineDTO dto) {
        InventoryOrderPipeline order = new InventoryOrderPipeline();
        order.setOrderId(dto.getOrderId());
        order.setProductId(dto.getProductId());
        order.setLocationId(dto.getLocationId());
        order.setOrderType(dto.getOrderType());
        order.setOrderedQty(dto.getOrderedQty());

        order.setSupplierName(dto.getSupplierName());
        order.setStatus(dto.getStatus());
        order.setCreatedBy(dto.getCreatedBy());
        return order;
    }

    private void updateEntityFromDto(InventoryOrderPipelineDTO dto, InventoryOrderPipeline order) {
        order.setProductId(dto.getProductId());
        order.setLocationId(dto.getLocationId());
        order.setOrderType(dto.getOrderType());
        order.setOrderedQty(dto.getOrderedQty());
        order.setSupplierName(dto.getSupplierName());

        order.setStatus(dto.getStatus());
    }
}