package com.onified.distribute.service;

import com.onified.distribute.dto.LeadTimeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LeadTimeService {
    LeadTimeDTO createLeadTime(LeadTimeDTO leadTimeDto);
    LeadTimeDTO updateLeadTime(String id, LeadTimeDTO leadTimeDto);
    LeadTimeDTO getLeadTimeById(String id);
    Page<LeadTimeDTO> getAllLeadTimes(Pageable pageable);
    Page<LeadTimeDTO> getActiveLeadTimes(Pageable pageable);
    LeadTimeDTO getActiveLeadTimeByProductAndLocation(String productId, String locationId);
    Page<LeadTimeDTO> getLeadTimesByProduct(String productId, Pageable pageable);
    Page<LeadTimeDTO> getLeadTimesByLocation(String locationId, Pageable pageable);
    List<LeadTimeDTO> getLeadTimesByProductAndLocationIds(List<String> productIds, List<String> locationIds);
    LeadTimeDTO activateLeadTime(String id);
    LeadTimeDTO deactivateLeadTime(String id);
    void deleteLeadTime(String id);
    Double calculateTotalLeadTime(String productId, String locationId);
    LeadTimeDTO getLeadTimeByProductAndLocation(String productId, String locationId);
}
