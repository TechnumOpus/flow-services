package com.onified.distribute.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Document(collection = "inventory_orders_pipeline")
@CompoundIndex(name = "productId_locationId_status", def = "{'product_id': 1, 'location_id': 1, 'status': 1}")
@CompoundIndex(name = "status_expectedReceiptDate", def = "{'status': 1, 'expected_receipt_date': 1}")
@CompoundIndex(name = "locationId_status", def = "{'location_id': 1, 'status': 1}")
@CompoundIndex(name = "expectedReceiptDate_status", def = "{'expected_receipt_date': 1, 'status': 1}")
public class InventoryOrderPipeline {
    @Id
    private String id;

    @Indexed(unique = true)
    @Field("order_id")
    private String orderId;

    @Indexed
    @Field("product_id")
    private String productId;

    @Indexed
    @Field("location_id")
    private String locationId;

    @Indexed
    @Field("supplier_location_id")
    private String supplierLocationId;

    @Field("order_type")
    private String orderType;

    @Field("ordered_qty")
    private Integer orderedQty;

    @Field("received_qty")
    private Integer receivedQty;

    @Field("pending_qty")
    private Integer pendingQty;

    @Field("order_date")
    private LocalDateTime orderDate;

    @Field("expected_receipt_date")
    private LocalDateTime expectedReceiptDate;

    @Field("actual_receipt_date")
    private LocalDateTime actualReceiptDate;

    @Field("external_order_ref")
    private String externalOrderRef;

    @Field("supplier_ref")
    private String supplierRef;

    private String status;
    private String priority;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("created_by")
    private String createdBy;

    @Field("last_status_change")
    private LocalDateTime lastStatusChange;

    @Field("last_status_change_by")
    private String lastStatusChangeBy;
}

