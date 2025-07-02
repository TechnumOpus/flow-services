package com.onified.distribute.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "seasonality_adjustment")
@CompoundIndex(name = "productId_locationId_month_isActive", def = "{'productId': 1, 'locationId': 1, 'month': 1, 'isActive': 1}")
@CompoundIndex(name = "category_month_isActive", def = "{'category': 1, 'month': 1, 'isActive': 1}")
@CompoundIndex(name = "month_isActive", def = "{'month': 1, 'isActive': 1}")
public class SeasonalityAdjustment {
    @Id
    private String id;
    private String adjustmentId;
    private String productId;
    private String locationId;
    @Indexed
    private String category;
    @Indexed
    private Integer month;
    private Double seasonalityFactor;
    private Integer year;
    private String description;
    private String confidenceLevel;
    @Indexed
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
