package com.onified.distribute.repository;

import com.onified.distribute.entity.ReplenishmentQueue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReplenishmentQueueRepository extends MongoRepository<ReplenishmentQueue, String> {

    @Query("{ " +
            "$and: [" +
            "  { $or: [ { 'productId': { $regex: ?0, $options: 'i' } }, { ?0: null } ] }," +
            "  { $or: [ { 'locationId': { $regex: ?1, $options: 'i' } }, { ?1: null } ] }," +
            "  { $or: [ { 'bufferZone': { $regex: ?2, $options: 'i' } }, { ?2: null } ] }," +
            "  { $or: [ { 'status': { $regex: ?3, $options: 'i' } }, { ?3: null } ] }," +
            "  { $or: [ { 'bufferGap': { $gte: ?4 } }, { ?4: null } ] }," +
            "  { $or: [ { 'bufferGap': { $lte: ?5 } }, { ?5: null } ] }," +
            "  { $or: [ { 'daysOfSupply': { $gte: ?6 } }, { ?6: null } ] }," +
            "  { $or: [ { 'daysOfSupply': { $lte: ?7 } }, { ?7: null } ] }," +
            "  { $or: [ { 'recommendedAction': { $regex: ?8, $options: 'i' } }, { ?8: null } ] }," +
            "  { $or: [ { 'priorityScore': { $gte: ?9 } }, { ?9: null } ] }," +
            "  { $or: [ { 'priorityScore': { $lte: ?10 } }, { ?10: null } ] }," +
            "  { 'isActive': true }" +
            "] }")
    Page<ReplenishmentQueue> findByFilters(
            String productId,
            String locationId,
            String bufferZone,
            String status,
            Integer minBufferGap,
            Integer maxBufferGap,
            Double minDaysOfSupply,
            Double maxDaysOfSupply,
            String recommendedAction,
            Double minPriorityScore,
            Double maxPriorityScore,
            Pageable pageable
    );

    @Query("{ 'productId': { $in: ?0 }, 'isActive': true }")
    Page<ReplenishmentQueue> findByProductIdIn(List<String> productIds, Pageable pageable);

    @Query("{ 'locationId': { $in: ?0 }, 'isActive': true }")
    Page<ReplenishmentQueue> findByLocationIdIn(List<String> locationIds, Pageable pageable);

    @Query("{ 'bufferGap': { $gte: ?0, $lte: ?1 }, 'isActive': true }")
    Page<ReplenishmentQueue> findByBufferGapBetween(Integer minGap, Integer maxGap, Pageable pageable);


    Page<ReplenishmentQueue> findByIsActiveTrue(Pageable pageable);

    ReplenishmentQueue findByOrderId(String orderId);
    // Find by Product and Location
    Page<ReplenishmentQueue> findByProductIdAndLocationId(String productId, String locationId, Pageable pageable);

    // Find by Product ID
    Page<ReplenishmentQueue> findByProductId(String productId, Pageable pageable);

    // Find by Location ID
    Page<ReplenishmentQueue> findByLocationId(String locationId, Pageable pageable);

    ReplenishmentQueue findByQueueId(String queueId);

    @Query("{'status': ?0, 'isActive': true}")
    Page<ReplenishmentQueue> findByStatusAndIsActiveTrue(String status, Pageable pageable);

    @Query("{'bufferZone': ?0, 'status': ?1, 'isActive': true}")
    Page<ReplenishmentQueue> findByBufferZoneAndStatusAndIsActiveTrue(String bufferZone, String status, Pageable pageable);
}
