package com.onified.distribute.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InventoryOrderPipelineDTO {
    private String id;
    private String orderId;
    private String productId;
    private String locationId;
    private String orderType;
    private Integer orderedQty;

    private LocalDateTime orderDate;
    private LocalDateTime expectedReceiptDate;
    private LocalDateTime actualReceiptDate;
    private String externalOrderRef;
    private String supplierName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;


}
