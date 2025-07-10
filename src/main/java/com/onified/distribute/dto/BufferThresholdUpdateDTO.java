package com.onified.distribute.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BufferThresholdUpdateDTO {

    @DecimalMin(value = "0.0", message = "Red threshold percentage must be non-negative")
    @DecimalMax(value = "100.0", message = "Red threshold percentage cannot exceed 100")
    private Double redThresholdPct;

    @DecimalMin(value = "0.0", message = "Yellow threshold percentage must be non-negative")
    @DecimalMax(value = "100.0", message = "Yellow threshold percentage cannot exceed 100")
    private Double yellowThresholdPct;

    private String reviewCycleId;

    @Min(value = 1, message = "Review period days must be at least 1")
    private Integer dbmReviewPeriodDays;

    private String reviewAutomation; // "System" or "Manual"

    // Optional field for change trigger percentage
    private Double changeTriggerPct;

    // For audit purposes
    private String updatedBy;
    private String comments;
}
