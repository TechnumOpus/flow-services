package com.onified.distribute.controller;

import com.onified.distribute.dto.request.CreateOrdersRequestDTO;
import com.onified.distribute.dto.InventoryOrderPipelineDTO;
import com.onified.distribute.service.order.InventoryOrderPipelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class InventoryOrderPipelineController {

    private final InventoryOrderPipelineService orderService;

    @GetMapping("/pending")
    public ResponseEntity<Page<InventoryOrderPipelineDTO>> getPendingOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderId,asc") String sort,
            @RequestParam(required = false) String locationId) {
        log.info("Fetching pending orders - page: {}, size: {}, sort: {}, locationId: {}", page, size, sort, locationId);
        String[] sortParams = sort.split(",");
        Sort.Direction direction = Sort.Direction.fromString(sortParams[1]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        Page<InventoryOrderPipelineDTO> orders = orderService.getPendingOrders(locationId, pageable);
        return ResponseEntity.ok(orders);
    }
    @PostMapping("/approve-all")
    public ResponseEntity<Map<String, Object>> approveSelectedOrders(@RequestBody List<String> orderIds) {
        log.info("Approving selected orders: {}", orderIds);
        if (orderIds == null || orderIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Order IDs must be provided"));
        }
        orderService.approveSelectedOrders(orderIds, "SYSTEM"); // Default userId
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Successfully approved orders: " + orderIds);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<Page<InventoryOrderPipelineDTO>> getActiveOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderId,asc") String sort,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String locationId) {
        log.info("Fetching active orders - page: {}, size: {}, sort: {}, status: {}, locationId: {}",
                page, size, sort, status, locationId);
        String[] sortParams = sort.split(",");
        Sort.Direction direction = Sort.Direction.fromString(sortParams[1]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        Page<InventoryOrderPipelineDTO> orders = orderService.getActiveOrders(status, locationId, pageable);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/bulk")
    public ResponseEntity<Void> createBulkOrders(@Valid @RequestBody CreateOrdersRequestDTO requestDTO) {
        log.info("Creating bulk orders for {} queue items", requestDTO.getQueueItems().size());
        String userId = "SYSTEM"; // Default userId since authentication is removed
        orderService.createBulkOrders(requestDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/orders")
    public ResponseEntity<InventoryOrderPipelineDTO> createOrder(@Valid @RequestBody InventoryOrderPipelineDTO orderDTO) {
        log.info("Received request to create order with productId: {}, locationId: {}",
                orderDTO.getProductId(), orderDTO.getLocationId());
        InventoryOrderPipelineDTO createdOrder = orderService.createOrder(orderDTO);
        log.info("Successfully created order with ID: {}", createdOrder.getOrderId());
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<InventoryOrderPipelineDTO> updateOrder(
            @PathVariable String orderId,
            @Valid @RequestBody InventoryOrderPipelineDTO orderDTO) {
        log.info("Updating order: {}", orderId);
        InventoryOrderPipelineDTO updatedOrder = orderService.updateOrder(orderId, orderDTO);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<InventoryOrderPipelineDTO> getOrderById(@PathVariable String orderId) {
        log.info("Fetching order: {}", orderId);
        InventoryOrderPipelineDTO order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/by-status-and-location")
    public ResponseEntity<Page<InventoryOrderPipelineDTO>> getOrdersByStatusAndLocation(
            @RequestParam String status,
            @RequestParam String locationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching orders by status: {} and location: {}", status, locationId);
        Pageable pageable = PageRequest.of(page, size);
        Page<InventoryOrderPipelineDTO> orders = orderService.getOrdersByStatusAndLocation(status, locationId, pageable);
        return ResponseEntity.ok(orders);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String orderId) {
        log.info("Deleting order: {}", orderId);
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}