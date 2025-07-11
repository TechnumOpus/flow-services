package com.onified.distribute.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class BufferAdjustmentLogDTO {
    private String logId;
    private String bufferId;
    private String productId;
    private String locationId;
    private String adjustmentType;
    private Integer proposedBufferUnits;
    private Integer safetyBufferUnits;
    private Integer finalBufferUnits;
    private Double changePercentage;
    private String triggerReason;
    private Integer consecutiveDaysInZone;
    private Double baseADC;
    private Boolean systemRecommended;
    private Boolean requiresApproval;
    private String approvalStatus;
    private String approvedBy;
    private LocalDateTime approvalDate;
    private LocalDateTime adjustmentDate;
    private String createdBy;
    private String comments;
}
