package com.onified.distribute.controller;

import com.onified.distribute.dto.DailyConsumptionLogDTO;
import com.onified.distribute.service.DailyConsumptionLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/daily-consumption-logs")
@RequiredArgsConstructor
@Validated
public class DailyConsumptionLogController {

    private final DailyConsumptionLogService dailyConsumptionLogService;

    @PostMapping
    public ResponseEntity<DailyConsumptionLogDTO> createDailyConsumptionLog(@Valid @RequestBody DailyConsumptionLogDTO logDto) {
        log.info("Creating daily consumption log for product: {} at location: {} for date: {}", 
                logDto.getProductId(), logDto.getLocationId(), logDto.getConsumptionDate());
        
        DailyConsumptionLogDTO createdLog = dailyConsumptionLogService.createDailyConsumptionLog(logDto);
        return new ResponseEntity<>(createdLog, HttpStatus.CREATED);
    }

    @PutMapping("/{logId}")
    public ResponseEntity<DailyConsumptionLogDTO> updateDailyConsumptionLog(
            @PathVariable String logId,
            @Valid @RequestBody DailyConsumptionLogDTO logDto) {
        log.info("Updating daily consumption log: {}", logId);
        
        DailyConsumptionLogDTO updatedLog = dailyConsumptionLogService.updateDailyConsumptionLog(logId, logDto);
        return ResponseEntity.ok(updatedLog);
    }

    @GetMapping("/{logId}")
    public ResponseEntity<DailyConsumptionLogDTO> getDailyConsumptionLogById(@PathVariable String logId) {
        log.info("Fetching daily consumption log: {}", logId);
        
        DailyConsumptionLogDTO log = dailyConsumptionLogService.getDailyConsumptionLogById(logId);
        return ResponseEntity.ok(log);
    }

    @GetMapping
    public ResponseEntity<Page<DailyConsumptionLogDTO>> getAllDailyConsumptionLogs(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "consumptionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("Fetching all daily consumption logs - page: {}, size: {}", page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<DailyConsumptionLogDTO> logs = dailyConsumptionLogService.getAllDailyConsumptionLogs(pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<DailyConsumptionLogDTO>> getDailyConsumptionLogsByProduct(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        log.info("Fetching daily consumption logs by product: {}", productId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("consumptionDate").descending());
        Page<DailyConsumptionLogDTO> logs = dailyConsumptionLogService.getDailyConsumptionLogsByProduct(productId, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<Page<DailyConsumptionLogDTO>> getDailyConsumptionLogsByLocation(
            @PathVariable String locationId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        log.info("Fetching daily consumption logs by location: {}", locationId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("consumptionDate").descending());
        Page<DailyConsumptionLogDTO> logs = dailyConsumptionLogService.getDailyConsumptionLogsByLocation(locationId, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/product/{productId}/location/{locationId}")
    public ResponseEntity<Page<DailyConsumptionLogDTO>> getDailyConsumptionLogsByProductAndLocation(
            @PathVariable String productId,
            @PathVariable String locationId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        log.info("Fetching daily consumption logs by product: {} and location: {}", productId, locationId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("consumptionDate").descending());
        Page<DailyConsumptionLogDTO> logs = dailyConsumptionLogService.getDailyConsumptionLogsByProductAndLocation(
                productId, locationId, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/date-range")
    public ResponseEntity<Page<DailyConsumptionLogDTO>> getDailyConsumptionLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        log.info("Fetching daily consumption logs between {} and {}", startDate, endDate);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("consumptionDate").descending());
        Page<DailyConsumptionLogDTO> logs = dailyConsumptionLogService.getDailyConsumptionLogsByDateRange(
                startDate, endDate, pageable);
        return ResponseEntity.ok(logs);
    }

//    @GetMapping("/recent")
//    public ResponseEntity<List<DailyConsumptionLogDTO>> getRecentConsumption(
//            @RequestParam String productId,
//            @RequestParam String locationId,
//            @RequestParam @Min(1) Integer days) {
//        log.info("Fetching recent {} days consumption for product: {} at location: {}", days, productId, locationId);
//
//        List<DailyConsumptionLogDTO> logs = dailyConsumptionLogService.getRecentConsumption(productId, locationId, days);
//        return ResponseEntity.ok(logs);
//    }
//
//    @GetMapping("/latest")
//    public ResponseEntity<DailyConsumptionLogDTO> getLatestConsumption(
//            @RequestParam String productId,
//            @RequestParam String locationId) {
//        log.info("Fetching latest consumption for product: {} at location: {}", productId, locationId);
//
//        DailyConsumptionLogDTO log = dailyConsumptionLogService.getLatestConsumption(productId, locationId);
//        if (log != null) {
//            return ResponseEntity.ok(log);
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }

//        @GetMapping("/period")
//    public ResponseEntity<List<DailyConsumptionLogDTO>> getConsumptionForPeriod(
//            @RequestParam String productId,
//            @RequestParam String locationId,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
//        log.info("Fetching consumption for product: {} at location: {} between {} and {}",
//                productId, locationId, startDate, endDate);
//
//        List<DailyConsumptionLogDTO> logs = dailyConsumptionLogService.getConsumptionForPeriod(
//                productId, locationId, startDate, endDate);
//        return ResponseEntity.ok(logs);
//    }

    @GetMapping("/average")
    public ResponseEntity<Double> calculateAverageConsumption(
            @RequestParam String productId,
            @RequestParam String locationId,
            @RequestParam @Min(1) Integer days) {
        log.info("Calculating average consumption for {} days for product: {} at location: {}", 
                days, productId, locationId);
        
        Double averageConsumption = dailyConsumptionLogService.calculateAverageConsumption(productId, locationId, days);
        return ResponseEntity.ok(averageConsumption);
    }

    @DeleteMapping("/cleanup")
    public ResponseEntity<Void> deleteOldConsumptionLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cutoffDate) {
        log.info("Deleting consumption logs older than: {}", cutoffDate);
        
        dailyConsumptionLogService.deleteOldConsumptionLogs(cutoffDate);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> deleteDailyConsumptionLog(@PathVariable String logId) {
        log.info("Deleting daily consumption log: {}", logId);
        
        dailyConsumptionLogService.deleteDailyConsumptionLog(logId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkLogExists(
            @RequestParam String productId,
            @RequestParam String locationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime consumptionDate) {
        log.info("Checking if log exists for product: {} at location: {} for date: {}", 
                productId, locationId, consumptionDate);
        
        boolean exists = dailyConsumptionLogService.existsForDate(productId, locationId, consumptionDate);
        return ResponseEntity.ok(exists);
    }
}
