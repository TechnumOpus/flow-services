package com.onified.distribute.repository;

import com.onified.distribute.entity.ConsumptionProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsumptionProfileRepository extends MongoRepository<ConsumptionProfile, String> {

    // Find by Product and Location (unique combination)
    Optional<ConsumptionProfile> findByProductIdAndLocationId(String productId, String locationId);

    // Find by Product ID
    Page<ConsumptionProfile> findByProductId(String productId, Pageable pageable);

    // Find by Location ID
    Page<ConsumptionProfile> findByLocationId(String locationId, Pageable pageable);

    // Find by ADC Trend
    Page<ConsumptionProfile> findByAdcTrend(String adcTrend, Pageable pageable);
    boolean existsByProductIdAndLocationId(String productId, String locationId);

    @Query("{'lastCalculatedAt': {$lt: ?0}}")
    Page<ConsumptionProfile> findProfilesNeedingRecalculation(java.time.LocalDateTime cutoffDate, Pageable pageable);
}
