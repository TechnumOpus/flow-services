package com.onified.distribute.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SeasonalityAdjustmentDTO {
    private String id;
    private String adjustmentId;
    private String productId;
    private String locationId;
    private String category;
    private Integer month;
    private Double seasonalityFactor;
    private Integer year;
    private String description;
    private String confidenceLevel;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
