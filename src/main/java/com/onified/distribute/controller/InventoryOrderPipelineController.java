package com.onified.distribute.controller;

import com.onified.distribute.dto.InventoryOrderPipelineDTO;
import com.onified.distribute.service.InventoryOrderPipelineService;
import com.onified.distribute.service.ReplenishmentQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/inventory-orders")
@RequiredArgsConstructor
public class InventoryOrderPipelineController {

    private final InventoryOrderPipelineService inventoryOrderPipelineService;
    private final ReplenishmentQueueService replenishmentQueueService;
    @PutMapping("/{orderId}/receipt")
    public ResponseEntity<InventoryOrderPipelineDTO> updateOrderReceipt(
            @PathVariable String orderId,
            @RequestParam Integer receivedQty,
            @RequestParam(required = false) LocalDateTime actualReceiptDate) {
        log.info("Updating receipt for order: {}", orderId);
        InventoryOrderPipelineDTO updatedOrder = inventoryOrderPipelineService.updateOrderReceipt(orderId, receivedQty,
                actualReceiptDate);
        return ResponseEntity.ok(updatedOrder);
    }
    @GetMapping("/in-transit/{locationId}")
    public ResponseEntity<Page<InventoryOrderPipelineDTO>> getInTransitOrders(
            @PathVariable String locationId, Pageable pageable) {
        log.info("Fetching in-transit orders for location: {}", locationId);
        Page<InventoryOrderPipelineDTO> orders = replenishmentQueueService.getInTransitOrders(locationId, pageable);
        return ResponseEntity.ok(orders);
    }

    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable String orderId) {
        log.info("Canceling order: {}", orderId);
        replenishmentQueueService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<InventoryOrderPipelineDTO> createOrder(@Valid @RequestBody InventoryOrderPipelineDTO orderDTO) {
        log.info("Creating inventory order with ID: {}", orderDTO.getOrderId());
        InventoryOrderPipelineDTO createdOrder = inventoryOrderPipelineService.createOrder(orderDTO);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<InventoryOrderPipelineDTO> updateOrder(
            @PathVariable String orderId,
            @Valid @RequestBody InventoryOrderPipelineDTO orderDTO) {
        log.info("Updating inventory order: {}", orderId);
        InventoryOrderPipelineDTO updatedOrder = inventoryOrderPipelineService.updateOrder(orderId, orderDTO);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<InventoryOrderPipelineDTO> getOrderById(@PathVariable String orderId) {
        log.info("Fetching inventory order: {}", orderId);
        InventoryOrderPipelineDTO order = inventoryOrderPipelineService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<Page<InventoryOrderPipelineDTO>> getOrdersByStatusAndLocation(
            @RequestParam String status,
            @RequestParam String locationId,
            Pageable pageable) {
        log.info("Fetching orders by status: {} and location: {}", status, locationId);
        Page<InventoryOrderPipelineDTO> orders = inventoryOrderPipelineService.getOrdersByStatusAndLocation(status, locationId, pageable);
        return ResponseEntity.ok(orders);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String orderId) {
        log.info("Deleting inventory order: {}", orderId);
        inventoryOrderPipelineService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}
