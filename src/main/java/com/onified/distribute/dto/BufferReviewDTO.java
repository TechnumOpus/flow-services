package com.onified.distribute.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BufferReviewDTO {
    private String bufferId;
    private String productId;
    private String locationId;
    private String productName;
    private String locationName;
    private LocalDateTime lastReviewDate;
    private Integer consecutiveZoneDays;
    private Integer currentBufferUnits;
    private Integer recommendedBufferUnits;
    private LocalDateTime nextReviewDue;
    private String suggestedAction;
    private String currentZone;
    private Double bufferConsumedPct;
    private Integer adjustmentThresholdDays;
    private Integer dbmReviewPeriodDays;
    private String triggerReason;
    private Double changePercentage;
    private Boolean requiresApproval;
    private Double baseADC;
    private Double rlt;
    private Integer netAvailableQty;
    private Integer currentInventory;
    private Integer inPipelineQty;
    private String adcTrend;
    private Double trendConfidence;
    private LocalDateTime calculationDate;
}
