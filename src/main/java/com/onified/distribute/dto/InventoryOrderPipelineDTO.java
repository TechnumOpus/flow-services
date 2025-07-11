package com.onified.distribute.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InventoryOrderPipelineDTO {
    private String id;
    private String orderId;
    private String productId;
    private String locationId;
    private String supplierLocationId;
    private String orderType;
    private Integer orderedQty;
    private Integer receivedQty;
    private Integer pendingQty;
    private LocalDateTime orderDate;
    private LocalDateTime expectedReceiptDate;
    private LocalDateTime actualReceiptDate;
    private String externalOrderRef;
    private String supplierRef;
    private String status;
    private String priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private LocalDateTime lastStatusChange;
    private String lastStatusChangeBy;


    // Computed field methods
    public Integer calculateOrderedQty(Integer bufferUnits, Integer currentInventory) {
        if (bufferUnits == null || currentInventory == null) {
            return 0;
        }
        return Math.max(0, bufferUnits - currentInventory);
    }

    public Integer calculatePendingQty() {
        if (orderedQty == null || receivedQty == null) {
            return orderedQty != null ? orderedQty : 0;
        }
        return Math.max(0, orderedQty - receivedQty);
    }

    public String calculatePriority(String currentZone, long orderCount) {
        double zoneWeight = switch (currentZone != null ? currentZone.toLowerCase() : "green") {
            case "red", "critical" -> 100.0;
            case "yellow" -> 66.0;
            case "green" -> 33.0;
            default -> 10.0;
        };
        double frequencyWeight = orderCount * 5.0; // 5 points per order
        double priorityScore = zoneWeight + frequencyWeight;
        return switch ((int) (priorityScore / 50)) {
            case 0 -> "LOW";
            case 1 -> "MEDIUM";
            case 2 -> "HIGH";
            default -> "CRITICAL";
        };
    }
}
