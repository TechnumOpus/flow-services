package com.onified.distribute.service;

import com.onified.distribute.dto.ReplenishmentOverrideLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReplenishmentOverrideLogService {
    void createOverrideLog(ReplenishmentOverrideLogDTO overrideLogDTO);
    Page<ReplenishmentOverrideLogDTO> getAllOverrideLogs(Pageable pageable);
    Page<ReplenishmentOverrideLogDTO> getOverrideLogsByProductId(String productId, Pageable pageable);
    Page<ReplenishmentOverrideLogDTO> getOverrideLogsByLocationId(String locationId, Pageable pageable);
    Page<ReplenishmentOverrideLogDTO> getOverrideLogsByProductIdAndLocationId(String productId, String locationId, Pageable pageable);
}