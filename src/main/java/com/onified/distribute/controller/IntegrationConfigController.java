package com.onified.distribute.controller;

import com.onified.distribute.dto.IntegrationConfigDTO;
import com.onified.distribute.service.IntegrationConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/integration-configs")
@RequiredArgsConstructor
public class IntegrationConfigController {

    private final IntegrationConfigService integrationConfigService;

    @PostMapping
    public ResponseEntity<IntegrationConfigDTO> createIntegrationConfig(@Valid @RequestBody IntegrationConfigDTO configDTO) {
        log.info("Creating integration config with ID: {}", configDTO.getConfigId());
        IntegrationConfigDTO createdConfig = integrationConfigService.createIntegrationConfig(configDTO);
        return new ResponseEntity<>(createdConfig, HttpStatus.CREATED);
    }

    @PutMapping("/{configId}")
    public ResponseEntity<IntegrationConfigDTO> updateIntegrationConfig(
            @PathVariable String configId,
            @Valid @RequestBody IntegrationConfigDTO configDTO) {
        log.info("Updating integration config: {}", configId);
        IntegrationConfigDTO updatedConfig = integrationConfigService.updateIntegrationConfig(configId, configDTO);
        return ResponseEntity.ok(updatedConfig);
    }

    @GetMapping("/{configId}")
    public ResponseEntity<IntegrationConfigDTO> getIntegrationConfigById(@PathVariable String configId) {
        log.info("Fetching integration config: {}", configId);
        IntegrationConfigDTO config = integrationConfigService.getIntegrationConfigById(configId);
        return ResponseEntity.ok(config);
    }

    @GetMapping
    public ResponseEntity<Page<IntegrationConfigDTO>> getAllIntegrationConfigs(Pageable pageable) {
        log.info("Fetching all integration configs");
        Page<IntegrationConfigDTO> configs = integrationConfigService.getAllIntegrationConfigs(pageable);
        return ResponseEntity.ok(configs);
    }

    @DeleteMapping("/{configId}")
    public ResponseEntity<Void> deleteIntegrationConfig(@PathVariable String configId) {
        log.info("Deleting integration config: {}", configId);
        integrationConfigService.deleteIntegrationConfig(configId);
        return ResponseEntity.noContent().build();
    }
}
