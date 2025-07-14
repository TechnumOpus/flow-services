package com.onified.distribute.service.impl.consumption;

import com.onified.distribute.dto.DailyConsumptionLogDTO;
import com.onified.distribute.entity.DailyConsumptionLog;
import com.onified.distribute.repository.DailyConsumptionLogRepository;
import com.onified.distribute.service.consumption.DailyConsumptionLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DailyConsumptionLogServiceImpl implements DailyConsumptionLogService {

    private final DailyConsumptionLogRepository dailyConsumptionLogRepository;

    @Override
    public DailyConsumptionLogDTO createDailyConsumptionLog(DailyConsumptionLogDTO logDto) {
        log.info("Creating daily consumption log for product: {} at location: {} for date: {}",
                logDto.getProductId(), logDto.getLocationId(), logDto.getConsumptionDate());
        
        if (existsForDate(logDto.getProductId(), logDto.getLocationId(), logDto.getConsumptionDate())) {
            throw new IllegalArgumentException("Consumption log already exists for this product, location and date");
        }
        
        DailyConsumptionLog consumptionLog = new DailyConsumptionLog();
        BeanUtils.copyProperties(logDto, consumptionLog);
        consumptionLog.setLogId(UUID.randomUUID().toString());
        consumptionLog.setCreatedAt(LocalDateTime.now());
        
        DailyConsumptionLog savedLog = dailyConsumptionLogRepository.save(consumptionLog);
        return convertToDto(savedLog);
    }

    @Override
    public DailyConsumptionLogDTO updateDailyConsumptionLog(String logId, DailyConsumptionLogDTO logDto) {
        log.info("Updating daily consumption log: {}", logId);
        
        DailyConsumptionLog existingLog = dailyConsumptionLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("Daily consumption log not found with ID: " + logId));
        
        BeanUtils.copyProperties(logDto, existingLog, "id", "logId", "createdAt");
        
        DailyConsumptionLog savedLog = dailyConsumptionLogRepository.save(existingLog);
        return convertToDto(savedLog);
    }

    @Override
    @Transactional(readOnly = true)
    public DailyConsumptionLogDTO getDailyConsumptionLogById(String logId) {
        log.info("Fetching daily consumption log: {}", logId);
        
        DailyConsumptionLog consumptionLog = dailyConsumptionLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("Daily consumption log not found with ID: " + logId));
        
        return convertToDto(consumptionLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DailyConsumptionLogDTO> getAllDailyConsumptionLogs(Pageable pageable) {
        log.info("Fetching all daily consumption logs");
        return dailyConsumptionLogRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DailyConsumptionLogDTO> getDailyConsumptionLogsByProduct(String productId, Pageable pageable) {
        log.info("Fetching daily consumption logs by product: {}", productId);
        return dailyConsumptionLogRepository.findByProductId(productId, pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DailyConsumptionLogDTO> getDailyConsumptionLogsByLocation(String locationId, Pageable pageable) {
        log.info("Fetching daily consumption logs by location: {}", locationId);
        return dailyConsumptionLogRepository.findByLocationId(locationId, pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DailyConsumptionLogDTO> getDailyConsumptionLogsByProductAndLocation(String productId, String locationId, Pageable pageable) {
        log.info("Fetching daily consumption logs by product: {} and location: {}", productId, locationId);
        return dailyConsumptionLogRepository.findByProductIdAndLocationId(productId, locationId, pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DailyConsumptionLogDTO> getDailyConsumptionLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.info("Fetching daily consumption logs between {} and {}", startDate, endDate);
        return dailyConsumptionLogRepository.findByConsumptionDateBetween(startDate, endDate, pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyConsumptionLogDTO> getRecentConsumption(String productId, String locationId, Integer days) {
        log.info("Fetching recent {} days consumption for product: {} at location: {}", days, productId, locationId);
        
        LocalDateTime fromDateTime = LocalDateTime.now().minusDays(days);
        List<DailyConsumptionLog> consumptionLogs = dailyConsumptionLogRepository.findRecentConsumption(productId, locationId, fromDateTime);
        
        return consumptionLogs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DailyConsumptionLogDTO getLatestConsumption(String productId, String locationId) {
        log.info("Fetching latest consumption for product: {} at location: {}", productId, locationId);

        List<DailyConsumptionLog> logs = dailyConsumptionLogRepository
                .findLatestConsumptionByProductAndLocationWithLimit(
                        productId,
                        locationId,
                        PageRequest.of(0, 1)
                );

        return logs.isEmpty() ? null : convertToDto(logs.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyConsumptionLogDTO> getConsumptionForPeriod(String productId, String locationId,
                                                               LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching consumption for product: {} at location: {} between {} and {}",
                productId, locationId, startDate, endDate);
        
        List<DailyConsumptionLog> consumptionLogs = dailyConsumptionLogRepository.findConsumptionForPeriod(
                productId, locationId, startDate, endDate);
        
        return consumptionLogs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateAverageConsumption(String productId, String locationId, Integer days) {
        log.info("Calculating average consumption for {} days for product: {} at location: {}",
                days, productId, locationId);
        
        List<DailyConsumptionLogDTO> recentConsumption = getRecentConsumption(productId, locationId, days);
        
        if (recentConsumption.isEmpty()) {
            return 0.0;
        }
        
        double totalConsumption = recentConsumption.stream()
                .mapToDouble(log -> log.getQuantityConsumed() != null ? log.getQuantityConsumed().doubleValue() : 0.0)
                .sum();
        
        return totalConsumption / recentConsumption.size();
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateTotalConsumption(String productId, String locationId, Integer days) {
        log.info("Calculating total consumption for {} days for product: {} at location: {}",
                days, productId, locationId);
        
        List<DailyConsumptionLogDTO> recentConsumption = getRecentConsumption(productId, locationId, days);
        
        return recentConsumption.stream()
                .mapToDouble(log -> log.getQuantityConsumed() != null ? log.getQuantityConsumed().doubleValue() : 0.0)
                .sum();
    }

    @Override
    public void deleteOldConsumptionLogs(LocalDateTime cutoffDate) {
        log.info("Deleting consumption logs older than: {}", cutoffDate);
        dailyConsumptionLogRepository.deleteByConsumptionDateBefore(cutoffDate);
    }

    @Override
    public void deleteDailyConsumptionLog(String logId) {
        log.info("Deleting daily consumption log: {}", logId);
        
        if (!dailyConsumptionLogRepository.existsById(logId)) {
            throw new IllegalArgumentException("Daily consumption log not found with ID: " + logId);
        }
        
        dailyConsumptionLogRepository.deleteById(logId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsForDate(String productId, String locationId, LocalDateTime consumptionDate) {
        return dailyConsumptionLogRepository.existsByProductIdAndLocationIdAndConsumptionDate(
                productId, locationId, consumptionDate);
    }

    private DailyConsumptionLogDTO convertToDto(DailyConsumptionLog log) {
        DailyConsumptionLogDTO dto = new DailyConsumptionLogDTO();
        // Map fields (e.g., using BeanUtils.copyProperties or manual mapping)
        dto.setLogId(log.getLogId());
        dto.setProductId(log.getProductId());
        dto.setLocationId(log.getLocationId());
        dto.setConsumptionDate(log.getConsumptionDate());
        dto.setQuantityConsumed(log.getQuantityConsumed());
        dto.setTransactionType(log.getTransactionType());
        dto.setTransactionRef(log.getTransactionRef());
        dto.setSourceSystem(log.getSourceSystem());
        dto.setCreatedAt(log.getCreatedAt());
        dto.setCreatedBy(log.getCreatedBy());
        return dto;
    }
}
