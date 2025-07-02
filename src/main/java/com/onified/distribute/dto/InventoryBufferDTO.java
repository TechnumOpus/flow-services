package com.onified.distribute.dto;

import lombok.Data;
import java.time.LocalDateTime;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class InventoryBufferDTO {
    private String id;

    @NotBlank(message = "Buffer ID is required")
    private String bufferId;

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Location ID is required")
    private String locationId;

    @NotBlank(message = "Buffer type is required")
    private String bufferType;

    @Min(value = 0, message = "Buffer days must be non-negative")
    private Integer bufferDays;


    private Integer bufferLeadTimeDays;

    @Min(value = 0, message = "Buffer units must be non-negative")
    private Integer bufferUnits;

    @Min(value = 0, message = "Green threshold percentage must be non-negative")
    private Double greenThresholdPct;

    @Min(value = 0, message = "Yellow threshold percentage must be non-negative")
    private Double yellowThresholdPct;

    @Min(value = 0, message = "Red threshold percentage must be non-negative")
    private Double redThresholdPct;

    @Min(value = 0, message = "Current inventory must be non-negative")
    private Integer currentInventory;

    @Min(value = 0, message = "In-pipeline quantity must be non-negative")
    private Integer inPipelineQty;

    private Integer netAvailableQty;
    private Double bufferConsumedPct;
    private String currentZone;
    private String reviewCycleId;

    @Min(value = 1, message = "DBM review period days must be at least 1")
    private Integer dbmReviewPeriodDays;

    private LocalDateTime lastReviewDate;
    private LocalDateTime nextReviewDue;

    @Min(value = 0, message = "Consecutive zone days must be non-negative")
    private Integer consecutiveZoneDays;

    @Min(value = 0, message = "Adjustment threshold days must be non-negative")
    private Integer adjustmentThresholdDays;

    @NotNull(message = "Active status is required")
    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
