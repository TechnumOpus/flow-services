package com.onified.distribute.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReplenishmentQueueDTO {
    private String id;
    private String queueId;
    private String productId;
    private String locationId;
    private Integer currentInventory;
    private Integer inPipelineQty;
    private Integer allocatedQty;
    private Integer netAvailableQty;
    private Integer bufferUnits;
    private Integer bufferGap;
    private String bufferZone;
    private Double daysOfSupply;
    private Integer recommendedQty;
    private String recommendedAction;
    private Double priorityScore;
    private Double adcUsed;
    private Double leadTimeDays;
    private List<String> reasonCodes;
    private LocalDateTime queueDate;
    private String processedBy;
    private LocalDateTime processedAt;
    private String actionTaken;
    private String orderId;
    private String status;
    private Boolean isActive;
}
