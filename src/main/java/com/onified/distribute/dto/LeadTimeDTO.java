package com.onified.distribute.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LeadTimeDTO {
    private String id;
    private String productId;
    private String locationId;
    private String supplierId;
    private Double orderLeadTime;
    private Double manufacturingTime;
    private Double transportTime;
    private Integer moq;
    private Boolean isActive;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
