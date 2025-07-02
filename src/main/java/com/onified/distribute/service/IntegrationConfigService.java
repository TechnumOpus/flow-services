package com.onified.distribute.service;

import com.onified.distribute.dto.IntegrationConfigDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IntegrationConfigService {
    IntegrationConfigDTO createIntegrationConfig(IntegrationConfigDTO configDTO);
    IntegrationConfigDTO updateIntegrationConfig(String configId, IntegrationConfigDTO configDTO);
    IntegrationConfigDTO getIntegrationConfigById(String configId);
    Page<IntegrationConfigDTO> getAllIntegrationConfigs(Pageable pageable);
    void deleteIntegrationConfig(String configId);
}