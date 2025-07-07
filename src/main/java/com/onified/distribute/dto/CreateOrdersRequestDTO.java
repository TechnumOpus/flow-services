package com.onified.distribute.dto;

import lombok.Data;

import jakarta.validation.constraints.*;

import java.util.List;

@Data
public class CreateOrdersRequestDTO {

    @NotNull(message = "Queue items are required")
    private List<QueueItem> queueItems;

    @Data
    public static class QueueItem {
        @NotBlank(message = "Queue ID is required")
        private String queueId;

        @NotBlank(message = "Product ID is required")
        private String productId;

        @NotBlank(message = "Location ID is required")
        private String locationId;

        @Min(value = 0, message = "Final quantity must be non-negative")
        private Integer finalQuantity;

        private String overrideReason;

        private String approver;
    }
}