package com.onified.distribute.service.impl;

import com.onified.distribute.dto.InventoryOrderPipelineDTO;
import com.onified.distribute.entity.InventoryOrderPipeline;
import com.onified.distribute.exception.BadRequestException;
import com.onified.distribute.exception.ResourceNotFoundException;
import com.onified.distribute.repository.InventoryOrderPipelineRepository;
import com.onified.distribute.service.InventoryOrderPipelineService;
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
public class InventoryOrderPipelineServiceImpl implements InventoryOrderPipelineService {
    
    private final InventoryOrderPipelineRepository orderRepository;

    @Override
    public InventoryOrderPipelineDTO updateOrderReceipt(String orderId, Integer receivedQty,
                                                        LocalDateTime actualReceiptDate) {
        log.info("Updating order receipt for order: {}", orderId);

        InventoryOrderPipeline order = orderRepository.findByOrderId(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found with ID: " + orderId);
        }

        if (receivedQty != null) {
            order.setReceivedQty(receivedQty);
            order.setPendingQty(order.getOrderedQty() - receivedQty);
        }
        if (actualReceiptDate != null) {
            order.setActualReceiptDate(actualReceiptDate);
        }
        order.setStatus("received");
        order.setUpdatedAt(LocalDateTime.now());
        order.setLastStatusChange(LocalDateTime.now());

        InventoryOrderPipeline savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }


    @Override
    public InventoryOrderPipelineDTO createOrder(InventoryOrderPipelineDTO orderDTO) {
        log.info("Creating inventory order pipeline with ID: {}", orderDTO.getOrderId());
        
        if (orderRepository.findByOrderId(orderDTO.getOrderId()) != null) {
            throw new BadRequestException("Order ID already exists: " + orderDTO.getOrderId());
        }
        
        InventoryOrderPipeline order = mapToEntity(orderDTO);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setLastStatusChange(LocalDateTime.now());
        order.setLastStatusChangeBy(orderDTO.getCreatedBy());        
        InventoryOrderPipeline savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
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
        existingOrder.setLastStatusChange(LocalDateTime.now());
        
        InventoryOrderPipeline savedOrder = orderRepository.save(existingOrder);
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
        return orderRepository.findByStatusAndLocationId(status, locationId, pageable)
                .map(this::convertToDto);
    }

    @Override
    public void deleteOrder(String orderId) {
        log.info("Deleting inventory order pipeline: {}", orderId);
        
        InventoryOrderPipeline order = orderRepository.findByOrderId(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found with ID: " + orderId);
        }
        
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    private InventoryOrderPipelineDTO convertToDto(InventoryOrderPipeline order) {
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
        dto.setExternalOrderRef(order.getExternalOrderRef());
        dto.setSupplierRef(order.getSupplierRef());
        dto.setStatus(order.getStatus());
        dto.setPriority(order.getPriority());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setCreatedBy(order.getCreatedBy());
        dto.setLastStatusChange(order.getLastStatusChange());
        dto.setLastStatusChangeBy(order.getLastStatusChangeBy());
        return dto;
    }

    private InventoryOrderPipeline mapToEntity(InventoryOrderPipelineDTO dto) {
        InventoryOrderPipeline order = new InventoryOrderPipeline();
        order.setOrderId(dto.getOrderId());
        order.setProductId(dto.getProductId());
        order.setLocationId(dto.getLocationId());
        order.setSupplierLocationId(dto.getSupplierLocationId());
        order.setOrderType(dto.getOrderType());
        order.setOrderedQty(dto.getOrderedQty());
        order.setReceivedQty(dto.getReceivedQty());
        order.setPendingQty(dto.getPendingQty());
        order.setOrderDate(dto.getOrderDate());
        order.setExpectedReceiptDate(dto.getExpectedReceiptDate());
        order.setActualReceiptDate(dto.getActualReceiptDate());
        order.setExternalOrderRef(dto.getExternalOrderRef());
        order.setSupplierRef(dto.getSupplierRef());
        order.setStatus(dto.getStatus());
        order.setPriority(dto.getPriority());
        order.setCreatedBy(dto.getCreatedBy());
        order.setLastStatusChangeBy(dto.getLastStatusChangeBy());
        return order;
    }

        private void updateEntityFromDto(InventoryOrderPipelineDTO dto, InventoryOrderPipeline order) {
        order.setProductId(dto.getProductId());
        order.setLocationId(dto.getLocationId());
        order.setSupplierLocationId(dto.getSupplierLocationId());
        order.setOrderType(dto.getOrderType());
        order.setOrderedQty(dto.getOrderedQty());
        order.setReceivedQty(dto.getReceivedQty());
        order.setPendingQty(dto.getPendingQty());
        order.setOrderDate(dto.getOrderDate());
        order.setExpectedReceiptDate(dto.getExpectedReceiptDate());
        order.setActualReceiptDate(dto.getActualReceiptDate());
        order.setExternalOrderRef(dto.getExternalOrderRef());
        order.setSupplierRef(dto.getSupplierRef());
        order.setStatus(dto.getStatus());
        order.setPriority(dto.getPriority());
        order.setLastStatusChangeBy(dto.getLastStatusChangeBy());
    }
}

