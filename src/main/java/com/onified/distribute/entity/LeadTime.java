package com.onified.distribute.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "lead_times")
@CompoundIndex(name = "product_location_active", def = "{'productId': 1, 'locationId': 1, 'isActive': 1}")
@CompoundIndex(name = "supplier_active", def = "{'supplierId': 1, 'isActive': 1}")
public class LeadTime {
    @Id
    private String id;

    @Indexed
    private String productId;

    @Indexed
    private String locationId;

    @Indexed
    private String supplierId;

    private Double orderLeadTime;
    private Double manufacturingTime;
    private Double transportTime;

    private Integer moq;


    @Indexed
    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
