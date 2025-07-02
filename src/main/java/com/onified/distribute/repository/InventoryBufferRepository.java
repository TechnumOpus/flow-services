package com.onified.distribute.repository;

import com.onified.distribute.entity.InventoryBuffer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryBufferRepository extends MongoRepository<InventoryBuffer, String> {

    // These Spring Data methods will work automatically now
    Optional<InventoryBuffer> findByProductIdAndLocationId(String productId, String locationId);
    Page<InventoryBuffer> findByProductId(String productId, Pageable pageable);
    Page<InventoryBuffer> findByLocationId(String locationId, Pageable pageable);
    Page<InventoryBuffer> findByCurrentZone(String currentZone, Pageable pageable);
    boolean existsByProductIdAndLocationId(String productId, String locationId);

    // Add this debug method to check specific fields
    @Query("{'_id': ?0}")
    Optional<InventoryBuffer> findByIdWithAllFields(String id);

    // Keep the @Query methods that work with specific field names
    @Query("{'next_review_due': {$lte: ?0}, 'is_active': true}")
    Page<InventoryBuffer> findBuffersDueForReview(LocalDateTime currentDate, Pageable pageable);

    @Query("{'location_id': ?0, 'is_active': true}")
    Page<InventoryBuffer> findActiveBuffersByLocationId(String locationId, Pageable pageable);

    @Query("{'is_active': true}")
    Page<InventoryBuffer> findActiveBuffers(Pageable pageable);

    @Query("{'current_zone': {$in: ?0}, 'is_active': true}")
    Page<InventoryBuffer> findByCurrentZoneIn(List<String> zones, Pageable pageable);

    @Query("{'updated_at': {$lt: ?0}, 'is_active': true}")
    Page<InventoryBuffer> findBuffersNeedingUpdate(LocalDateTime cutoffTime, Pageable pageable);

    @Query("{'current_zone': ?0, 'consecutive_zone_days': {$gte: ?1}, 'is_active': true}")
    Page<InventoryBuffer> findBuffersInZoneForDuration(String zone, Integer minDays, Pageable pageable);

    @Query(value = "{'is_active': true}", fields = "{'location_id': 1}")
    List<InventoryBuffer> findDistinctLocationIds();

    // Debug methods - you can remove these later
    @Query("{'location_id': ?0}")
    Page<InventoryBuffer> findAllByLocationIdDebug(String locationId, Pageable pageable);
}
