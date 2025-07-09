package com.onified.distribute.repository;

import com.onified.distribute.entity.SpecialEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialEventRepository extends MongoRepository<SpecialEvent, String> {
    
    Optional<SpecialEvent> findByEventId(String eventId);
    
    boolean existsByEventId(String eventId);
    
    @Query("{'isActive': true}")
    Page<SpecialEvent> findActiveEvents(Pageable pageable);
    
    Page<SpecialEvent> findByProductId(String productId, Pageable pageable);
    
    Page<SpecialEvent> findByLocationId(String locationId, Pageable pageable);
    

    @Query("{'startDate': {$lte: ?0}, 'endDate': {$gte: ?0}, 'isActive': true}")
    Page<SpecialEvent> findCurrentEvents(LocalDateTime currentDate, Pageable pageable);
    
    @Query("{'startDate': {$gt: ?0}, 'isActive': true}")
    Page<SpecialEvent> findUpcomingEvents(LocalDateTime currentDate, Pageable pageable);
    

    @Query("{'productId': ?0, 'locationId': ?1, $or: [" +
           "{'startDate': {$lte: ?2}, 'endDate': {$gte: ?2}}, " +
           "{'startDate': {$lte: ?3}, 'endDate': {$gte: ?3}}, " +
           "{'startDate': {$gte: ?2}, 'endDate': {$lte: ?3}}" +
           "]}")
    List<SpecialEvent> findOverlappingEvents(String productId, String locationId, LocalDateTime startDate, LocalDateTime endDate);
    
    List<SpecialEvent> findByProductIdAndLocationId(String productId, String locationId);
    

}
