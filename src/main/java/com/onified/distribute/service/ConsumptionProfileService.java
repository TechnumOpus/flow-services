package com.onified.distribute.service;

import com.onified.distribute.dto.ConsumptionProfileDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface ConsumptionProfileService {
    
    ConsumptionProfileDTO createConsumptionProfile(ConsumptionProfileDTO profileDto);
    
    ConsumptionProfileDTO updateConsumptionProfile(String profileId, ConsumptionProfileDTO profileDto);
    
    ConsumptionProfileDTO getConsumptionProfileById(String profileId);
    
    ConsumptionProfileDTO getConsumptionProfileByProductAndLocation(String productId, String locationId);
    
    Page<ConsumptionProfileDTO> getAllConsumptionProfiles(Pageable pageable);
    
    Page<ConsumptionProfileDTO> getConsumptionProfilesByProduct(String productId, Pageable pageable);
    
    Page<ConsumptionProfileDTO> getConsumptionProfilesByLocation(String locationId, Pageable pageable);
    
    Page<ConsumptionProfileDTO> getConsumptionProfilesByTrend(String adcTrend, Pageable pageable);

    ConsumptionProfileDTO calculateConsumptionProfile(String productId, String locationId);

    ConsumptionProfileDTO recalculateProfile(String profileId);

    Page<ConsumptionProfileDTO> getProfilesNeedingRecalculation(LocalDateTime cutoffDate, Pageable pageable);
    
    void deleteConsumptionProfile(String profileId);
    
    boolean existsByProductAndLocation(String productId, String locationId);
}
