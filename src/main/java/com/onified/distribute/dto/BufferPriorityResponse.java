package com.onified.distribute.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BufferPriorityResponse {
    private String bufferId;
    private String productId;
    private String locationId;
    private String currentZone;
    private Double bufferConsumedPct;
    private Integer consecutiveZoneDays;
    private Integer netAvailableQty;
    private Integer bufferUnits;
    private Integer missingUnits;
    private Integer priority;
    private String replenishmentUrgency;

    public Integer getMissingUnits() {
        if (bufferUnits == null || netAvailableQty == null) {
            return 0;
        }
        return Math.max(0, bufferUnits - netAvailableQty);
    }

    public String getReplenishmentUrgency() {
        if ("RED".equals(currentZone)) {
            return "CRITICAL";
        } else if ("YELLOW".equals(currentZone)) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
}
