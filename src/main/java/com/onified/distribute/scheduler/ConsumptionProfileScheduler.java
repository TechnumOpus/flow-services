package com.onified.distribute.scheduler;

import com.onified.distribute.dto.*;
import com.onified.distribute.entity.*;
import com.onified.distribute.repository.DailyConsumptionLogRepository;
import com.onified.distribute.service.ConsumptionProfileService;
import com.onified.distribute.service.DailyConsumptionLogService;
import com.onified.distribute.service.SeasonalityAdjustmentService;
import com.onified.distribute.service.SpecialEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumptionProfileScheduler {

    private final DailyConsumptionLogRepository dailyConsumptionLogRepository;
    private final ConsumptionProfileService consumptionProfileService;
    private final DailyConsumptionLogService dailyConsumptionLogService;
    private final SeasonalityAdjustmentService seasonalityAdjustmentService;
    private final SpecialEventService specialEventService;
    private final MongoTemplate mongoTemplate;

    @Scheduled(cron = "0 37 17 * * ?")
    @Transactional
    public void recalculateConsumptionProfiles() {
        log.info("Starting scheduled recalculation of consumption profiles at {}",
                ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));

        List<ProductLocationPair> pairs = getDistinctProductLocationPairs();
        log.info("Found {} distinct product-location pairs", pairs.size());

        for (ProductLocationPair pair : pairs) {
            try {
                recalculateProfileForPair(pair.getProductId(), pair.getLocationId());
            } catch (Exception e) {
                log.error("Error recalculating profile for product: {} and location: {}",
                        pair.getProductId(), pair.getLocationId(), e);
            }
        }

        log.info("Completed scheduled recalculation of consumption profiles");
    }

    private void recalculateProfileForPair(String productId, String locationId) {
        log.info("Recalculating consumption profile for product: {} at location: {}", productId, locationId);


        List<DailyConsumptionLogDTO> last7Days = dailyConsumptionLogService.getRecentConsumption(productId, locationId, 7);
        List<DailyConsumptionLogDTO> last14Days = dailyConsumptionLogService.getRecentConsumption(productId, locationId, 14);
        List<DailyConsumptionLogDTO> last30Days = dailyConsumptionLogService.getRecentConsumption(productId, locationId, 30);
        List<DailyConsumptionLogDTO> last60Days = dailyConsumptionLogService.getRecentConsumption(productId, locationId, 60);

        log.info("Fetched {} logs for 7 days, {} for 14 days, {} for 30 days, {} for 60 days",
                last7Days.size(), last14Days.size(), last30Days.size(), last60Days.size());

        Double adc7d = calculateADC(last7Days);
        Double adc14d = calculateADC(last14Days);
        Double adc30d = calculateADC(last30Days);
        Double adc60d = calculateADC(last60Days);

        Integer currentMonth = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).getMonthValue();
        Double seasonalityFactor = seasonalityAdjustmentService.getSeasonalityFactor(productId, locationId, currentMonth);
        Double eventImpactFactor = specialEventService.getEventImpactFactor(productId, locationId,
                ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDateTime());

        Double adcNormalized = adc30d * (seasonalityFactor + eventImpactFactor);

        Double coefficientOfVariation = calculateCoefficientOfVariation(last30Days);
        Double stdDeviation = calculateStandardDeviation(last30Days);

        String adcTrend = determineTrend(adc7d, adc14d, adc30d);
        Double trendConfidence = calculateTrendConfidence(adc7d, adc14d, adc30d);

        DailyConsumptionLogDTO latestConsumption = dailyConsumptionLogService.getLatestConsumption(productId, locationId);
        LocalDateTime lastConsumptionDate = latestConsumption != null ? latestConsumption.getConsumptionDate() : null;

        ConsumptionProfileDTO profileDto = new ConsumptionProfileDTO();
        profileDto.setProductId(productId);
        profileDto.setLocationId(locationId);
        profileDto.setAdc7d(adc7d);
        profileDto.setAdc14d(adc14d);
        profileDto.setAdc30d(adc30d);
        profileDto.setAdc60d(adc60d);
        profileDto.setAdcNormalized(adcNormalized);
        profileDto.setAdcTrend(adcTrend);
        profileDto.setTrendConfidence(trendConfidence);
        profileDto.setCoefficientOfVariation(coefficientOfVariation);
        profileDto.setStdDeviation(stdDeviation);
        profileDto.setDataPointsUsed(last30Days.size());
        profileDto.setLastConsumptionDate(lastConsumptionDate);
        profileDto.setCalculationDate(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDateTime());
        profileDto.setUpdatedAt(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDateTime());

        if (consumptionProfileService.existsByProductAndLocation(productId, locationId)) {
            ConsumptionProfileDTO existingProfileDto = consumptionProfileService
                    .getConsumptionProfileByProductAndLocation(productId, locationId);
            consumptionProfileService.updateConsumptionProfile(existingProfileDto.getId(), profileDto);
        } else {
            consumptionProfileService.createConsumptionProfile(profileDto);
        }

        verifyUpdatedProfile(productId, locationId);
    }

    private List<ProductLocationPair> getDistinctProductLocationPairs() {
        Query query = new Query();
        query.fields().include("productId", "locationId");
        List<DailyConsumptionLog> logs = mongoTemplate.find(query, DailyConsumptionLog.class);
        return logs.stream()
                .map(log -> new ProductLocationPair(log.getProductId(), log.getLocationId()))
                .distinct()
                .collect(Collectors.toList());
    }

    private Double calculateADC(List<DailyConsumptionLogDTO> consumptionLogs) {
        if (consumptionLogs.isEmpty()) {
            return 0.0;
        }
        double totalConsumption = consumptionLogs.stream()
                .mapToDouble(log -> log.getQuantityConsumed() != null ? log.getQuantityConsumed() : 0.0)
                .sum();
        return totalConsumption / consumptionLogs.size();
    }

    private String determineTrend(Double adc7d, Double adc14d, Double adc30d) {
        if (adc7d == null || adc14d == null || adc30d == null) {
            return "STABLE";
        }
        double shortTermTrend = (adc7d - adc14d) / adc14d * 100;
        double longTermTrend = (adc14d - adc30d) / adc30d * 100;
        if (shortTermTrend > 10 && longTermTrend > 5) {
            return "INCREASING";
        } else if (shortTermTrend < -10 && longTermTrend < -5) {
            return "DECREASING";
        } else if (Math.abs(shortTermTrend) > 20) {
            return "VOLATILE";
        } else {
            return "STABLE";
        }
    }

    private Double calculateTrendConfidence(Double adc7d, Double adc14d, Double adc30d) {
        if (adc7d == null || adc14d == null || adc30d == null) {
            return 0.0;
        }
        double variance = Math.pow(adc7d - adc30d, 2) + Math.pow(adc14d - adc30d, 2);
        double maxVariance = Math.pow(adc30d, 2) * 2;
        return Math.max(0.0, Math.min(1.0, 1.0 - (variance / maxVariance)));
    }

    private Double calculateCoefficientOfVariation(List<DailyConsumptionLogDTO> consumptionLogs) {
        if (consumptionLogs.size() < 2) {
            return 0.0;
        }
        double mean = calculateADC(consumptionLogs);
        if (mean == 0) {
            return 0.0;
        }
        double stdDev = calculateStandardDeviation(consumptionLogs);
        return (stdDev / mean) * 100;
    }

    private Double calculateStandardDeviation(List<DailyConsumptionLogDTO> consumptionLogs) {
        if (consumptionLogs.size() < 2) {
            return 0.0;
        }
        double mean = calculateADC(consumptionLogs);
        double sumSquaredDiffs = consumptionLogs.stream()
                .mapToDouble(log -> {
                    double value = log.getQuantityConsumed() != null ? log.getQuantityConsumed() : 0.0;
                    return Math.pow(value - mean, 2);
                })
                .sum();
        return Math.sqrt(sumSquaredDiffs / (consumptionLogs.size() - 1));
    }

    private void verifyUpdatedProfile(String productId, String locationId) {
        try {
            ConsumptionProfileDTO profile = consumptionProfileService.getConsumptionProfileByProductAndLocation(productId, locationId);
            log.info("Verified updated profile for product: {} at location: {}. ADC_7d: {}, ADC_30d: {}, ADC_Normalized: {}",
                    productId, locationId, profile.getAdc7d(), profile.getAdc30d(), profile.getAdcNormalized());
        } catch (Exception e) {
            log.error("Error verifying profile for product: {} at location: {}", productId, locationId, e);
        }
    }

    private static class ProductLocationPair {
        private String productId;
        private String locationId;

        public ProductLocationPair(String productId, String locationId) {
            this.productId = productId;
            this.locationId = locationId;
        }

        public String getProductId() {
            return productId;
        }

        public String getLocationId() {
            return locationId;
        }
    }
}