package com.onified.distribute.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "product_attributes")
@CompoundIndex(name = "productId_attributeName_locationId", def = "{'productId': 1, 'attributeName': 1, 'locationId': 1}")
@CompoundIndex(name = "productId_effectiveFrom_effectiveTo", def = "{'productId': 1, 'effectiveFrom': 1, 'effectiveTo': 1}")
public class ProductAttribute {
    @Id
    private String id;
    private String attributeId;
    private String productId;
    @Indexed
    private String attributeName;
    private String attributeValue;
    private String attributeCategory;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
