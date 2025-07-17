package com.onified.distribute.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BufferOverriddenResponse {

    private String productId;
    private String locationId;
    private String overriddenAction;
    private Integer oldBufferUnits;
    private Integer newBufferUnits;
    private Double changePercentage;
    private String adjustmentType;
    private String triggerReason;
    private String approvedBy;
    private LocalDateTime approvalDate;
}
