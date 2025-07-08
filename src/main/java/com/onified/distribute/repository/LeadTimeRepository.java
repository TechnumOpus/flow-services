package com.onified.distribute.repository;

import com.onified.distribute.entity.LeadTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadTimeRepository extends MongoRepository<LeadTime, String> {
    
   
    Page<LeadTime> findByIsActive(Boolean isActive, Pageable pageable);

    List<LeadTime> findByIsActiveTrue();
    
    Page<LeadTime> findByProductId(String productId, Pageable pageable);
    
    Page<LeadTime> findByLocationId(String locationId, Pageable pageable);
    
    Optional<LeadTime> findByProductIdAndLocationIdAndIsActive(String productId, String locationId, Boolean isActive);
    
    List<LeadTime> findByProductIdAndLocationId(String productId, String locationId);

    List<LeadTime> findByProductIdInAndLocationIdInAndIsActive(List<String> productIds, List<String> locationIds, boolean isActive);
}
