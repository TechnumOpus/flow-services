package com.onified.distribute.repository;

import com.onified.distribute.entity.BufferAdjustmentLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface BufferAdjustmentLogRepository extends MongoRepository<BufferAdjustmentLog, String> {

    // Basic finder methods
    Page<BufferAdjustmentLog> findByBufferId(String bufferId, Pageable pageable);
    
    Page<BufferAdjustmentLog> findByProductIdAndLocationId(String productId, String locationId, Pageable pageable);
    
    Page<BufferAdjustmentLog> findByApprovalStatus(String approvalStatus, Pageable pageable);
    
    Page<BufferAdjustmentLog> findByAdjustmentDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    Page<BufferAdjustmentLog> findByApprovedBy(String approvedBy, Pageable pageable);
    
    Page<BufferAdjustmentLog> findByCreatedBy(String createdBy, Pageable pageable);

    @Query("{'systemRecommended': false, 'overriddenAction': {$ne: null}, 'adjustmentType': {$in: ['MANUAL_OVERRIDE', 'THRESHOLD_UPDATE', 'MANUAL_BUFFER_ADJUSTMENT']}}")
    Page<BufferAdjustmentLog> findManualOverrideLogs(Pageable pageable);

    @Query("{'systemRecommended': false, 'overriddenAction': {$ne: null}, 'adjustmentType': {$in: ['MANUAL_OVERRIDE', 'THRESHOLD_UPDATE', 'MANUAL_BUFFER_ADJUSTMENT']}, 'productId': ?0}")
    Page<BufferAdjustmentLog> findManualOverrideLogsByProductId(String productId, Pageable pageable);

    @Query("{'systemRecommended': false, 'overriddenAction': {$ne: null}, 'adjustmentType': {$in: ['MANUAL_OVERRIDE', 'THRESHOLD_UPDATE', 'MANUAL_BUFFER_ADJUSTMENT']}, 'locationId': ?0}")
    Page<BufferAdjustmentLog> findManualOverrideLogsByLocationId(String locationId, Pageable pageable);

    @Query("{'systemRecommended': false, 'overriddenAction': {$ne: null}, 'adjustmentType': {$in: ['MANUAL_OVERRIDE', 'THRESHOLD_UPDATE', 'MANUAL_BUFFER_ADJUSTMENT']}, 'productId': ?0, 'locationId': ?1}")
    Page<BufferAdjustmentLog> findManualOverrideLogsByProductIdAndLocationId(String productId, String locationId, Pageable pageable);
    // Query-based methods
    @Query("{'approvalStatus': 'PENDING'}")
    Page<BufferAdjustmentLog> findPendingApprovals(Pageable pageable);
}
