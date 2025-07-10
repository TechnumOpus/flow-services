package com.onified.distribute.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class BufferCalculationResponseDTO {
    private String productId;
    private String locationId;
    private Double rlt; // replenishment lead time in days
    private Double adc7d; // 7adc
    private Double adc14d; // 14adc
    private Double adc30d; // 30adc
    private String baseADC; // echo back user's choice
    private Double safetyFactor; // as percentage
    private Double bufferUnits;
    private Double safetyBufferUnits; // quantity of units added as safety
    private Double finalQuantity;
    private String calculationStatus; // SUCCESS, PARTIAL_DATA, ERROR
    private String message; // Additional information
}
