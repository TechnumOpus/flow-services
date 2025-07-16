package com.onified.distribute.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;

@Data
@Document(collection = "products")
public class Product {
    @Id
    private String id;

    @Indexed(unique = true)
    private String productId;

    @Indexed(unique = true)
    private String supplierName;

    @Indexed(unique = true)
    private String tenantSku;

    @Indexed(unique = true)
    private String supplierSku;

    private String name;

    @Indexed
    private String category;

    private String subcategory;

    private String uom;


    @Indexed
    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;
}
