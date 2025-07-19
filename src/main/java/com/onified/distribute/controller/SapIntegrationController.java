package com.onified.distribute.controller;

import com.onified.distribute.service.sap.SapIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sap")
@RequiredArgsConstructor
@Slf4j
public class SapIntegrationController {

    private final SapIntegrationService sapIntegrationService;

    @PostMapping("/sync")
    public ResponseEntity<Map<String, String>> manualSync(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            log.info("Manual SAP sync triggered for date range: {} to {}", startDate, endDate);
            sapIntegrationService.syncDailyConsumptionData(startDate, endDate);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "SAP sync completed successfully for date range: " + startDate + " to " + endDate,
                    "startDate", startDate.toString(),
                    "endDate", endDate.toString()
            ));

        } catch (Exception e) {
            log.error("Error in manual SAP sync for date range: {} to {}", startDate, endDate, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "SAP sync failed: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/sync/yesterday")
    public ResponseEntity<Map<String, String>> syncYesterday() {
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            log.info("Manual SAP sync triggered for yesterday: {}", yesterday);
            sapIntegrationService.syncDailyConsumptionData(yesterday, yesterday);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "SAP sync completed successfully for date: " + yesterday,
                    "date", yesterday.toString()
            ));

        } catch (Exception e) {
            log.error("Error in yesterday's SAP sync", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "SAP sync failed: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/sync/status")
    public ResponseEntity<Map<String, String>> getSyncStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "available",
                "message", "SAP sync service is running"
        ));
    }
}