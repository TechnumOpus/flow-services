package com.onified.distribute.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BufferThresholdCycleDTO {
    private String bufferId;
    private String productId;
    private String locationId;
    private String type;
    private String typeName;
    private Double redThresholdPct;
    private Double yellowThresholdPct;
    private String reviewCycleId;
    private String reviewCycleName;
    private Integer dbmReviewPeriodDays;
    private String reviewDate;
    private Double changeTriggerPct;
    private String reviewAutomation;

    // Additional metadata for context
    private LocalDateTime lastReviewDate;
    private LocalDateTime nextReviewDue;
    private String currentZone;
    private Boolean isActive;
}
