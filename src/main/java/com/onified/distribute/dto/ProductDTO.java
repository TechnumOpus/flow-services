package com.onified.distribute.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

@Data
public class ProductDTO {
    private String id;

    private String productId;

    private String supplierName;

    private String tenantSku;

    private String supplierSku;

    private String name;

    private String category;

    private String subcategory;

    private String uom;


    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;
}
