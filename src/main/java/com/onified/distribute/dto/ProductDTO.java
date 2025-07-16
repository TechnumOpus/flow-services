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

    @NotBlank(message = "supplier name is required")
    private String supplierName;

    @NotBlank(message = "Tenant SKU is required")
    private String tenantSku;

    @NotBlank(message = "Supplier SKU is required")
    private String supplierSku;

    @NotBlank(message = "Product name is required")
    private String name;

    private String category;

    private String subcategory;

    @NotBlank(message = "UOM is required")
    private String uom;


    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;
}
