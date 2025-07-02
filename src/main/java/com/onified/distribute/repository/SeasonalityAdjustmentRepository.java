package com.onified.distribute.repository;

import com.onified.distribute.entity.SeasonalityAdjustment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeasonalityAdjustmentRepository extends MongoRepository<SeasonalityAdjustment, String> {
    
    Optional<SeasonalityAdjustment> findByAdjustmentId(String adjustmentId);
    
    boolean existsByAdjustmentId(String adjustmentId);
    
    Page<SeasonalityAdjustment> findByIsActive(Boolean isActive, Pageable pageable);
    
    Page<SeasonalityAdjustment> findByProductId(String productId, Pageable pageable);
    
    Page<SeasonalityAdjustment> findByLocationId(String locationId, Pageable pageable);
    
    Page<SeasonalityAdjustment> findByMonth(Integer month, Pageable pageable);
    

    List<SeasonalityAdjustment> findByProductIdAndLocationId(String productId, String locationId);
    
    List<SeasonalityAdjustment> findByProductIdAndLocationIdAndIsActive(String productId, String locationId, Boolean isActive);
    
    Optional<SeasonalityAdjustment> findByProductIdAndLocationIdAndMonthAndIsActive(String productId, String locationId, Integer month, Boolean isActive);
    

}
