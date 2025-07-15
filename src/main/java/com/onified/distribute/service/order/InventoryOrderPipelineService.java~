package com.onified.distribute.service.order;

import com.onified.distribute.dto.CreateOrdersRequestDTO;
import com.onified.distribute.dto.InventoryOrderPipelineDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryOrderPipelineService {
    InventoryOrderPipelineDTO createOrder(InventoryOrderPipelineDTO orderDTO);
    InventoryOrderPipelineDTO updateOrder(String orderId, InventoryOrderPipelineDTO orderDTO);
    InventoryOrderPipelineDTO updateOrderReceipt(String orderId, Integer receivedQty, LocalDateTime actualReceiptDate);
    InventoryOrderPipelineDTO getOrderById(String orderId);
    Page<InventoryOrderPipelineDTO> getOrdersByStatusAndLocation(String status, String locationId, Pageable pageable);
    void deleteOrder(String orderId);
    void createBulkOrders(CreateOrdersRequestDTO requestDTO, String userId);
    Page<InventoryOrderPipelineDTO> getPendingOrders(String locationId, Pageable pageable);
    void approveAllPendingOrders(String userId);
    Page<InventoryOrderPipelineDTO> getActiveOrders(String status, String locationId, Pageable pageable);
    void approveSelectedOrders(List<String> orderIds, String userId);
}
