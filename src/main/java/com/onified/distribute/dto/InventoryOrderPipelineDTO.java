package com.onified.distribute.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

@Data
public class InventoryOrderPipelineDTO {

    // Input fields (provided by the client)
    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Location ID is required")
    private String locationId;

    @NotBlank(message = "Order type is required")
    private String orderType;

    @NotNull(message = "Ordered quantity is required")
    @Positive(message = "Ordered quantity must be positive")
    private Integer orderedQty;

    @NotBlank(message = "Supplier name is required")
    private String supplierName;

    @NotBlank(message = "Status is required")
    private String status;

    // Output fields (set by the server)
    private String orderId; // Generated automatically (ORD-XXXX)
    private LocalDateTime createdAt; // Set by the server
    private LocalDateTime updatedAt; // Set by the server
    private String createdBy; // Optional, can be set based on authentication
}
//    private LocalDateTime orderDate;
//    private LocalDateTime expectedReceiptDate;
//    private LocalDateTime actualReceiptDate;
//    private String externalOrderRef;