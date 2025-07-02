package com.onified.distribute.service;

import com.onified.distribute.dto.InventoryOrderPipelineDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface InventoryOrderPipelineService {
    InventoryOrderPipelineDTO createOrder(InventoryOrderPipelineDTO orderDTO);
    InventoryOrderPipelineDTO updateOrder(String orderId, InventoryOrderPipelineDTO orderDTO);
    InventoryOrderPipelineDTO getOrderById(String orderId);
    Page<InventoryOrderPipelineDTO> getOrdersByStatusAndLocation(String status, String locationId, Pageable pageable);
    void deleteOrder(String orderId);
    InventoryOrderPipelineDTO updateOrderReceipt(String orderId, Integer receivedQty, LocalDateTime actualReceiptDate);

}
