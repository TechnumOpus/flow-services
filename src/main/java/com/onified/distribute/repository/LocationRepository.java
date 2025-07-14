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
    
    boolean existsByLocationId(String locationId);
    
    Page<Location> findByIsActive(Boolean isActive, Pageable pageable);
    
    Page<Location> findByType(String type, Pageable pageable);


    Page<Location> findByRegion(String region, Pageable pageable);

    // For case-insensitive search
    Page<Location> findByRegionIgnoreCase(String region, Pageable pageable);

    Optional<Location> findByLocationId(String locationId);
    List<Location> findByLocationIdIn(List<String> locationIds);
    List<Location> findByIsActiveTrue();
    List<Location> findByRegionAndIsActive(String region, Boolean isActive);
    List<Location> findByTypeAndIsActive(String type, Boolean isActive);


    // For partial matching
    @Query("{'region': {$regex: ?0, $options: 'i'}}")
    Page<Location> findByRegionContainingIgnoreCase(String region, Pageable pageable);

}
