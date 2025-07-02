package com.onified.distribute.repository;

import com.onified.distribute.entity.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends MongoRepository<Location, String> {
    
    Optional<Location> findByLocationId(String locationId);
    boolean existsByLocationId(String locationId);
    
    Page<Location> findByIsActive(Boolean isActive, Pageable pageable);
    
    Page<Location> findByType(String type, Pageable pageable);
    
    Page<Location> findByRegion(String region, Pageable pageable);
    

    List<Location> findByLocationIdIn(List<String> locationIds);
    
}
