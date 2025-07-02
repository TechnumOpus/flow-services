package com.onified.distribute.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
@Data
@Document(collection = "inventory_buffer")  // Change to singular to match your MongoDB
@CompoundIndex(name = "productId_locationId", def = "{'product_id': 1, 'location_id': 1}", unique = true)
@CompoundIndex(name = "currentZone_isActive", def = "{'current_zone': 1, 'is_active': 1}")
@CompoundIndex(name = "locationId_currentZone", def = "{'location_id': 1, 'current_zone': 1}")
@CompoundIndex(name = "review_cycle_id", def = "{'review_cycle_id': 1}")
public class InventoryBuffer {

    @Id
    private String id;

    @Field("buffer_id")
    @Indexed(unique = true)
    private String bufferId;

    @Field("product_id")
    @Indexed
    private String productId;

    @Field("location_id")
    @Indexed
    private String locationId;

    @Field("buffer_type")
    private String bufferType;

    @Field("buffer_days")
    private Integer bufferDays;

    @Field("buffer_units")
    private Integer bufferUnits;

    @Field("green_threshold_pct")
    private Double greenThresholdPct;

    @Field("yellow_threshold_pct")
    private Double yellowThresholdPct;

    @Field("red_threshold_pct")
    private Double redThresholdPct;

    @Field("current_inventory")
    private Integer currentInventory;

    @Field("in_pipeline_qty")
    private Integer inPipelineQty;

    @Field("net_available_qty")
    private Integer netAvailableQty;

    @Field("buffer_consumed_pct")
    private Double bufferConsumedPct;

    @Field("current_zone")
    private String currentZone;

    @Field("review_cycle_id")
    @Indexed
    private String reviewCycleId;

    @Field("dbm_review_period_days")
    private Integer dbmReviewPeriodDays;

    @Field("last_review_date")
    private LocalDateTime lastReviewDate;

    @Field("next_review_due")
    @Indexed
    private LocalDateTime nextReviewDue;

    @Field("consecutive_zone_days")
    private Integer consecutiveZoneDays;

    @Field("adjustment_threshold_days")
    private Integer adjustmentThresholdDays;

    @Field("is_active")
    private Boolean isActive;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("created_by")
    private String createdBy;

    @Field("updated_by")
    private String updatedBy;
}

