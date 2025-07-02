package com.onified.distribute.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConsumptionProfileDTO {
    private String id;
    private String productId;
    private String locationId;
    private Double adc7d;
    private Double adc14d;
    private Double adc30d;
    private Double adc60d;
    private Double adcNormalized;
    private String adcTrend;
    private Double trendConfidence;
    private Double coefficientOfVariation;
    private Double stdDeviation;
    private LocalDateTime calculationDate;
    private Integer dataPointsUsed;
    private LocalDateTime lastConsumptionDate;
    private LocalDateTime updatedAt;
}
