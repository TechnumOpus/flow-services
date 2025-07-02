package com.onified.distribute.service.impl;

import com.onified.distribute.dto.ConsumptionProfileDTO;
import com.onified.distribute.dto.DailyConsumptionLogDTO;
import com.onified.distribute.entity.ConsumptionProfile;
import com.onified.distribute.repository.ConsumptionProfileRepository;
import com.onified.distribute.service.ConsumptionProfileService;
import com.onified.distribute.service.DailyConsumptionLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ConsumptionProfileServiceImpl implements ConsumptionProfileService {

    private final ConsumptionProfileRepository consumptionProfileRepository;
    private final DailyConsumptionLogService dailyConsumptionLogService;

    @Override
    public ConsumptionProfileDTO createConsumptionProfile(ConsumptionProfileDTO profileDto) {
        log.info("Creating consumption profile for product: {} at location: {}", 
                profileDto.getProductId(), profileDto.getLocationId());
        
        if (existsByProductAndLocation(profileDto.getProductId(), profileDto.getLocationId())) {
            throw new IllegalArgumentException("Consumption profile already exists for product and location combination");
        }
        
        ConsumptionProfile profile = new ConsumptionProfile();
        BeanUtils.copyProperties(profileDto, profile);
        profile.setCalculationDate(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        
        ConsumptionProfile savedProfile = consumptionProfileRepository.save(profile);
        return convertToDto(savedProfile);
    }

    @Override
    public ConsumptionProfileDTO updateConsumptionProfile(String profileId, ConsumptionProfileDTO profileDto) {
        log.info("Updating consumption profile: {}", profileId);
        
        ConsumptionProfile existingProfile = consumptionProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Consumption profile not found with ID: " + profileId));
        
        BeanUtils.copyProperties(profileDto, existingProfile, "id", "calculationDate");
        existingProfile.setUpdatedAt(LocalDateTime.now());
        
        ConsumptionProfile savedProfile = consumptionProfileRepository.save(existingProfile);
        return convertToDto(savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsumptionProfileDTO getConsumptionProfileById(String profileId) {
        log.info("Fetching consumption profile: {}", profileId);
        
        ConsumptionProfile profile = consumptionProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Consumption profile not found with ID: " + profileId));
        
        return convertToDto(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsumptionProfileDTO getConsumptionProfileByProductAndLocation(String productId, String locationId) {
        log.info("Fetching consumption profile for product: {} at location: {}", productId, locationId);
        
        ConsumptionProfile profile = consumptionProfileRepository.findByProductIdAndLocationId(productId, locationId)
                .orElseThrow(() -> new IllegalArgumentException("Consumption profile not found for product and location"));
        
        return convertToDto(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConsumptionProfileDTO> getAllConsumptionProfiles(Pageable pageable) {
        log.info("Fetching all consumption profiles");
        return consumptionProfileRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConsumptionProfileDTO> getConsumptionProfilesByProduct(String productId, Pageable pageable) {
        log.info("Fetching consumption profiles by product: {}", productId);
        return consumptionProfileRepository.findByProductId(productId, pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConsumptionProfileDTO> getConsumptionProfilesByLocation(String locationId, Pageable pageable) {
        log.info("Fetching consumption profiles by location: {}", locationId);
        return consumptionProfileRepository.findByLocationId(locationId, pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConsumptionProfileDTO> getConsumptionProfilesByTrend(String adcTrend, Pageable pageable) {
        log.info("Fetching consumption profiles by trend: {}", adcTrend);
        return consumptionProfileRepository.findByAdcTrend(adcTrend, pageable).map(this::convertToDto);
    }

    @Override
    public ConsumptionProfileDTO calculateConsumptionProfile(String productId, String locationId) {
        log.info("Calculating consumption profile for product: {} at location: {}", productId, locationId);

        // Get consumption data for different periods
        List<DailyConsumptionLogDTO> last7Days = dailyConsumptionLogService.getRecentConsumption(productId, locationId, 7);
        List<DailyConsumptionLogDTO> last14Days = dailyConsumptionLogService.getRecentConsumption(productId, locationId, 14);
        List<DailyConsumptionLogDTO> last30Days = dailyConsumptionLogService.getRecentConsumption(productId, locationId, 30);
        List<DailyConsumptionLogDTO> last60Days = dailyConsumptionLogService.getRecentConsumption(productId, locationId, 60);

        // Calculate ADC values
        Double adc7d = calculateADC(last7Days);
        Double adc14d = calculateADC(last14Days);
        Double adc30d = calculateADC(last30Days);
        Double adc60d = calculateADC(last60Days);

        // Calculate normalized ADC (using 30-day as base)
        Double adcNormalized = adc30d;

        // Determine trend
        String adcTrend = determineTrend(adc7d, adc14d, adc30d);
        Double trendConfidence = calculateTrendConfidence(adc7d, adc14d, adc30d);

        // Calculate variability metrics
        Double coefficientOfVariation = calculateCoefficientOfVariation(last30Days);
        Double stdDeviation = calculateStandardDeviation(last30Days);

        // Get latest consumption date
        DailyConsumptionLogDTO latestConsumption = dailyConsumptionLogService.getLatestConsumption(productId, locationId);
        LocalDateTime lastConsumptionDate = latestConsumption != null ? latestConsumption.getConsumptionDate() : null;

        // Create or update profile
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

        // Check if profile exists
        if (existsByProductAndLocation(productId, locationId)) {
            ConsumptionProfile existingProfile = consumptionProfileRepository.findByProductIdAndLocationId(productId, locationId).get();
            return updateConsumptionProfile(existingProfile.getId(), profileDto);
        } else {
            return createConsumptionProfile(profileDto);
        }
    }

    @Override
    public ConsumptionProfileDTO recalculateProfile(String profileId) {
        log.info("Recalculating consumption profile: {}", profileId);

        ConsumptionProfile existingProfile = consumptionProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Consumption profile not found with ID: " + profileId));

        return calculateConsumptionProfile(existingProfile.getProductId(), existingProfile.getLocationId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConsumptionProfileDTO> getProfilesNeedingRecalculation(LocalDateTime cutoffDate, Pageable pageable) {
        log.info("Fetching profiles needing recalculation before: {}", cutoffDate);
        return consumptionProfileRepository.findProfilesNeedingRecalculation(cutoffDate, pageable).map(this::convertToDto);
    }

    @Override
    public void deleteConsumptionProfile(String profileId) {
        log.info("Deleting consumption profile: {}", profileId);
        
        if (!consumptionProfileRepository.existsById(profileId)) {
            throw new IllegalArgumentException("Consumption profile not found with ID: " + profileId);
        }
        
        consumptionProfileRepository.deleteById(profileId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByProductAndLocation(String productId, String locationId) {
        return consumptionProfileRepository.existsByProductIdAndLocationId(productId, locationId);
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

    private ConsumptionProfileDTO convertToDto(ConsumptionProfile profile) {
        ConsumptionProfileDTO dto = new ConsumptionProfileDTO();
        BeanUtils.copyProperties(profile, dto);
        return dto;
    }
}
