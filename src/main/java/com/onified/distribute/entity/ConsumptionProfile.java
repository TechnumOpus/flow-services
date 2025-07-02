package com.onified.distribute.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "consumption_profile")
@CompoundIndex(name = "productId_locationId", def = "{'productId': 1, 'locationId': 1}", unique = true)
@CompoundIndex(name = "adcTrend_updatedAt", def = "{'adcTrend': 1, 'updatedAt': -1}")
public class ConsumptionProfile {
    @Id
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
    @Indexed
    private LocalDateTime calculationDate;
    private Integer dataPointsUsed;
    private LocalDateTime lastConsumptionDate;
    private LocalDateTime updatedAt;
}
