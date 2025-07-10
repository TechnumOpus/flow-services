package com.onified.distribute.repository;

import com.onified.distribute.entity.ReviewCycle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewCycleRepository extends MongoRepository<ReviewCycle, String> {
    
    Optional<ReviewCycle> findByCycleId(String cycleId);
    
    boolean existsByCycleId(String cycleId);
    
    Page<ReviewCycle> findByIsActive(Boolean isActive, Pageable pageable);
    
    Page<ReviewCycle> findByFrequency(String frequency, Pageable pageable);

    @Query("{'nextReviewDate': {$gte: ?0}, 'isActive': true}")
    List<ReviewCycle> findUpcomingReviewCycles(LocalDateTime date);

    List<ReviewCycle> findByIsActiveTrue();

}
