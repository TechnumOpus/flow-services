package com.onified.distribute.service.impl.config;

import com.onified.distribute.dto.IntegrationConfigDTO;
import com.onified.distribute.entity.IntegrationConfig;
import com.onified.distribute.exception.BadRequestException;
import com.onified.distribute.exception.ResourceNotFoundException;
import com.onified.distribute.repository.IntegrationConfigRepository;
import com.onified.distribute.service.config.IntegrationConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class IntegrationConfigServiceImpl implements IntegrationConfigService {
    
    private final IntegrationConfigRepository configRepository;

    @Override
    public IntegrationConfigDTO createIntegrationConfig(IntegrationConfigDTO configDTO) {
        log.info("Creating integration config with ID: {}", configDTO.getConfigId());
        
        if (configRepository.findByConfigId(configDTO.getConfigId()) != null) {
            throw new BadRequestException("Config ID already exists: " + configDTO.getConfigId());
        }
        
        IntegrationConfig config = mapToEntity(configDTO);
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        config.setIsActive(true);
        
        IntegrationConfig savedConfig = configRepository.save(config);
        return convertToDto(savedConfig);
    }

    @Override
    public IntegrationConfigDTO updateIntegrationConfig(String configId, IntegrationConfigDTO configDTO) {
        log.info("Updating integration config: {}", configId);
        
        IntegrationConfig existingConfig = configRepository.findByConfigId(configId);
        if (existingConfig == null) {
            throw new ResourceNotFoundException("Integration config not found with ID: " + configId);
        }
        
        updateEntityFromDto(configDTO, existingConfig);
        existingConfig.setUpdatedAt(LocalDateTime.now());
        
        IntegrationConfig savedConfig = configRepository.save(existingConfig);
        return convertToDto(savedConfig);
    }

    @Override
    @Transactional(readOnly = true)
    public IntegrationConfigDTO getIntegrationConfigById(String configId) {
        log.info("Fetching integration config: {}", configId);
        
        IntegrationConfig config = configRepository.findByConfigId(configId);
        if (config == null) {
            throw new ResourceNotFoundException("Integration config not found with ID: " + configId);
        }
        
        return convertToDto(config);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IntegrationConfigDTO> getAllIntegrationConfigs(Pageable pageable) {
        log.info("Fetching all active integration configs");
        return configRepository.findByIsActiveTrue(pageable).map(this::convertToDto);
    }

    @Override
    public void deleteIntegrationConfig(String configId) {
        log.info("Deleting integration config: {}", configId);
        
        IntegrationConfig config = configRepository.findByConfigId(configId);
        if (config == null) {
            throw new ResourceNotFoundException("Integration config not found with ID: " + configId);
        }
        
        config.setIsActive(false);
        config.setUpdatedAt(LocalDateTime.now());
        configRepository.save(config);
    }

    private IntegrationConfigDTO convertToDto(IntegrationConfig config) {
        IntegrationConfigDTO dto = new IntegrationConfigDTO();
        dto.setId(config.getId());
        dto.setConfigId(config.getConfigId());
        dto.setSystemName(config.getSystemName());
        dto.setIntegrationType(config.getIntegrationType());
        dto.setProductId(config.getProductId());
        dto.setLocationId(config.getLocationId());
        dto.setCategory(config.getCategory());
        dto.setEndpointUrl(config.getEndpointUrl());
        dto.setAuthenticationType(config.getAuthenticationType());
        dto.setSyncFrequency(config.getSyncFrequency());
        dto.setBatchSize(config.getBatchSize());
        dto.setLastSyncAt(config.getLastSyncAt());
        dto.setLastSuccessfulSync(config.getLastSuccessfulSync());
        dto.setLastErrorAt(config.getLastErrorAt());
        dto.setLastErrorMessage(config.getLastErrorMessage());
        dto.setSyncStatus(config.getSyncStatus());
        dto.setConsecutiveFailures(config.getConsecutiveFailures());
        dto.setFieldMappings(config.getFieldMappings());
        dto.setTransformationRules(config.getTransformationRules());
        dto.setIsActive(config.getIsActive());
        dto.setCreatedAt(config.getCreatedAt());
        dto.setUpdatedAt(config.getUpdatedAt());
        dto.setCreatedBy(config.getCreatedBy());
        return dto;
    }

    private IntegrationConfig mapToEntity(IntegrationConfigDTO dto) {
        IntegrationConfig config = new IntegrationConfig();
        config.setConfigId(dto.getConfigId());
        config.setSystemName(dto.getSystemName());
        config.setIntegrationType(dto.getIntegrationType());
        config.setProductId(dto.getProductId());
        config.setLocationId(dto.getLocationId());
        config.setCategory(dto.getCategory());
        config.setEndpointUrl(dto.getEndpointUrl());
        config.setAuthenticationType(dto.getAuthenticationType());
        config.setSyncFrequency(dto.getSyncFrequency());
        config.setBatchSize(dto.getBatchSize());
        config.setLastSyncAt(dto.getLastSyncAt());
        config.setLastSuccessfulSync(dto.getLastSuccessfulSync());
        config.setLastErrorAt(dto.getLastErrorAt());
        config.setLastErrorMessage(dto.getLastErrorMessage());
        config.setSyncStatus(dto.getSyncStatus());
        config.setConsecutiveFailures(dto.getConsecutiveFailures());
        config.setFieldMappings(dto.getFieldMappings());
        config.setTransformationRules(dto.getTransformationRules());
        config.setIsActive(dto.getIsActive());
        config.setCreatedBy(dto.getCreatedBy());
        return config;
    }

    private void updateEntityFromDto(IntegrationConfigDTO dto, IntegrationConfig config) {
        config.setSystemName(dto.getSystemName());
        config.setIntegrationType(dto.getIntegrationType());
        config.setProductId(dto.getProductId());
        config.setLocationId(dto.getLocationId());
        config.setCategory(dto.getCategory());
        config.setEndpointUrl(dto.getEndpointUrl());
        config.setAuthenticationType(dto.getAuthenticationType());
        config.setSyncFrequency(dto.getSyncFrequency());
        config.setBatchSize(dto.getBatchSize());
        config.setLastSyncAt(dto.getLastSyncAt());
        config.setLastSuccessfulSync(dto.getLastSuccessfulSync());
        config.setLastErrorAt(dto.getLastErrorAt());
        config.setLastErrorMessage(dto.getLastErrorMessage());
        config.setSyncStatus(dto.getSyncStatus());
        config.setConsecutiveFailures(dto.getConsecutiveFailures());
        config.setFieldMappings(dto.getFieldMappings());
        config.setTransformationRules(dto.getTransformationRules());
        config.setIsActive(dto.getIsActive());
    }
}
