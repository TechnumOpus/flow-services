package com.onified.distribute.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductAttributeDTO {
    private String id;
    private String attributeId;
    private String productId;

    private String attributeName;
    private String attributeValue;
    private String attributeCategory;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
