package com.onified.distribute.repository;

import com.onified.distribute.entity.InventoryOrderPipeline;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryOrderPipelineRepository extends MongoRepository<InventoryOrderPipeline, String> {


    // Find orders by multiple statuses and location
    @Query("{'status': {$in: ?0}, 'locationId': ?1}")
    Page<InventoryOrderPipeline> findByStatusInAndLocationId(List<String> statuses, String locationId, Pageable pageable);

    // Find orders by product and location with specific statuses
    @Query("{'productId': ?0, 'locationId': ?1, 'status': {$in: ?2}}")
    List<InventoryOrderPipeline> findByProductIdAndLocationIdAndStatusIn(String productId, String locationId, List<String> statuses);

    // Get pipeline summary by location
    @Query(value = "{'locationId': ?0, 'status': {$in: ?1}}", fields = "{'productId': 1, 'pendingQty': 1}")
    List<InventoryOrderPipeline> findPipelineSummaryByLocationAndStatus(String locationId, List<String> statuses);

    // Find by Product ID
    Page<InventoryOrderPipeline> findByProductId(String productId, Pageable pageable);

    // Find by Location ID
    Page<InventoryOrderPipeline> findByLocationId(String locationId, Pageable pageable);

    InventoryOrderPipeline findByOrderId(String orderId);

    @Query("{'status': ?0, 'location_id': ?1}")
    Page<InventoryOrderPipeline> findByStatusAndLocationId(String status, String locationId, Pageable pageable);

    @Query("{'location_id': ?0}")
    Page<InventoryOrderPipeline> findAllByLocationIdDebug(String locationId, Pageable pageable);
}

