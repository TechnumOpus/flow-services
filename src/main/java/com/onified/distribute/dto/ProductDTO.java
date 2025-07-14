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

    @NotBlank(message = "SKU Code is required")
    private String skuCode;

    @NotBlank(message = "Tenant SKU is required")
    private String tenantSku;

    @NotBlank(message = "Supplier SKU is required")
    private String supplierSku;

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    private String subcategory;

    @NotBlank(message = "UOM is required")
    private String uom;

    @Positive(message = "MOQ must be positive")
    private Integer moq;

    @Positive(message = "Unit cost must be positive")
    private Double unitCost;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;
}
