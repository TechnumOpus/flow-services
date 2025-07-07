package com.onified.distribute.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class ReplenishmentOverrideLogDTO {

    private String id;

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Location ID is required")
    private String locationId;

    @Min(value = 0, message = "Original quantity must be non-negative")
    private Integer originalQuantity;

    @Min(value = 0, message = "Overridden quantity must be non-negative")
    private Integer overriddenQuantity;

    @NotBlank(message = "Reason is required")
    private String reason;

    @NotBlank(message = "Approver is required")
    private String approver;

    @NotNull(message = "Timestamp is required")
    private LocalDateTime timestamp;

    @NotBlank(message = "Created by is required")
    private String createdBy;
}