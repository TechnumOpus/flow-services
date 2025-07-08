package com.onified.distribute.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplenishmentQueueFilterDTO {
    private String productId;
    private String locationId;
    private String bufferZone;
    private String status;
    private Integer minBufferGap;
    private Integer maxBufferGap;
    private Double minDaysOfSupply;
    private Double maxDaysOfSupply;
    private String recommendedAction;
    private Double minPriorityScore;
    private Double maxPriorityScore;
}