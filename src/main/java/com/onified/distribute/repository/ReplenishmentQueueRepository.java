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
