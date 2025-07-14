package com.onified.distribute.dto.response;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Builder
public class LeadTimeResponseDTO {
    private String id;
    private String productId;
    private String locationId;
    private String location; // location name
    private String tenantSku;
    private String supplierSku;
    private String supplierName;
    private Integer moq;
    private Double orderLeadTime;
    private Double manufacturingTime;
    private Double transportTime;
    private Double totalLeadTimeDays; // calculated field
    private Double leadTimeVariability;
    private Double onTimeDeliveryPct;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private Boolean isActive;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
