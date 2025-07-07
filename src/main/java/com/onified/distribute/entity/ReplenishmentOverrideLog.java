package com.onified.distribute.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Document(collection = "replenishment_override_log")
@CompoundIndex(name = "productId_locationId_timestamp", def = "{'product_id': 1, 'location_id': 1, 'timestamp': -1}")
public class ReplenishmentOverrideLog {

    @Id
    private String id;

    @Indexed
    @Field("product_id")
    private String productId;

    @Indexed
    @Field("location_id")
    private String locationId;

    @Field("original_quantity")
    private Integer originalQuantity;

    @Field("overridden_quantity")
    private Integer overriddenQuantity;

    private String reason;

    private String approver;

    @Indexed
    private LocalDateTime timestamp;

    @Field("created_by")
    private String createdBy;
}