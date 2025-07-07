package com.onified.distribute.repository;

import com.onified.distribute.entity.ReplenishmentOverrideLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReplenishmentOverrideLogRepository extends MongoRepository<ReplenishmentOverrideLog, String> {

    @Query("{'product_id': ?0}")
    Page<ReplenishmentOverrideLog> findByProductId(String productId, Pageable pageable);

    @Query("{'location_id': ?0}")
    Page<ReplenishmentOverrideLog> findByLocationId(String locationId, Pageable pageable);

    @Query("{'product_id': ?0, 'location_id': ?1}")
    Page<ReplenishmentOverrideLog> findByProductIdAndLocationId(String productId, String locationId, Pageable pageable);
}