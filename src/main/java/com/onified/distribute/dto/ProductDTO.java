package com.onified.distribute.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductDTO {
    private String id;
    private String productId;
    private String skuCode;
    private String name;
    private String category;
    private String subcategory;
    private String uom;
    private Integer moq;
    private Double unitCost;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
