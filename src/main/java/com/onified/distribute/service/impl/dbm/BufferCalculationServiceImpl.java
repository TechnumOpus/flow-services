package com.onified.distribute.service.impl.dbm;

import com.onified.distribute.dto.request.BufferCalculationRequestDTO;
import com.onified.distribute.dto.response.BufferCalculationResponseDTO;
import com.onified.distribute.entity.ConsumptionProfile;
import com.onified.distribute.entity.LeadTime;
import com.onified.distribute.repository.ConsumptionProfileRepository;
import com.onified.distribute.repository.LeadTimeRepository;
import com.onified.distribute.service.dbm.BufferCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BufferCalculationServiceImpl implements BufferCalculationService {

    private final ConsumptionProfileRepository consumptionProfileRepository;
    private final LeadTimeRepository leadTimeRepository;

    @Override
    public BufferCalculationResponseDTO calculateBufferQuantity(BufferCalculationRequestDTO request) {
        log.info("Calculating buffer quantity for product: {} at location: {} with baseADC: {} and safetyFactor: {}%",
                request.getProductId(), request.getLocationId(), request.getBaseADC(), request.getSafetyFactor());

        try {
            // Fetch consumption profile
            Optional<ConsumptionProfile> consumptionProfileOpt =
                    consumptionProfileRepository.findByProductIdAndLocationId(request.getProductId(), request.getLocationId());

            // Fetch lead time
            Optional<LeadTime> leadTimeOpt =
                    leadTimeRepository.findByProductIdAndLocationIdAndIsActive(request.getProductId(), request.getLocationId(), true);

            // Check if we have required data
            if (consumptionProfileOpt.isEmpty() && leadTimeOpt.isEmpty()) {
                return createErrorResponse(request, "No consumption profile or lead time data found");
            }

            ConsumptionProfile profile = consumptionProfileOpt.orElse(null);
            LeadTime leadTime = leadTimeOpt.orElse(null);

            // Calculate RLT (Replenishment Lead Time)
            Double rlt = calculateRLT(leadTime);

            // Get base ADC value based on user selection
            Double baseADCValue = getBaseADCValue(profile, request.getBaseADC());

            // Perform calculations
            if (baseADCValue != null && rlt != null && rlt > 0) {
                return performCalculation(request, profile, rlt, baseADCValue);
            } else {
                return createPartialDataResponse(request, profile, rlt, "Insufficient data for calculation");
            }

        } catch (Exception e) {
            log.error("Error calculating buffer quantity for product: {} at location: {}",
                    request.getProductId(), request.getLocationId(), e);
            return createErrorResponse(request, "Calculation failed: " + e.getMessage());
        }
    }

    @Override
    public List<BufferCalculationResponseDTO> calculateBufferQuantitiesForLocation(String locationId,
                                                                                   String baseADC,
                                                                                   Double safetyFactor,
                                                                                   Pageable pageable) {
        log.info("Calculating buffer quantities for location: {} with baseADC: {} and safetyFactor: {}%",
                locationId, baseADC, safetyFactor);

        return consumptionProfileRepository.findByLocationId(locationId, pageable)
                .stream()
                .map(profile -> {
                    BufferCalculationRequestDTO request = new BufferCalculationRequestDTO();
                    request.setProductId(profile.getProductId());
                    request.setLocationId(locationId);
                    request.setBaseADC(baseADC);
                    request.setSafetyFactor(safetyFactor);
                    return calculateBufferQuantity(request);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<BufferCalculationResponseDTO> calculateBufferQuantitiesForProduct(String productId,
                                                                                  String baseADC,
                                                                                  Double safetyFactor,
                                                                                  Pageable pageable) {
        log.info("Calculating buffer quantities for product: {} with baseADC: {} and safetyFactor: {}%",
                productId, baseADC, safetyFactor);

        return consumptionProfileRepository.findByProductId(productId, pageable)
                .stream()
                .map(profile -> {
                    BufferCalculationRequestDTO request = new BufferCalculationRequestDTO();
                    request.setProductId(productId);
                    request.setLocationId(profile.getLocationId());
                    request.setBaseADC(baseADC);
                    request.setSafetyFactor(safetyFactor);
                    return calculateBufferQuantity(request);
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean canCalculateBuffer(String productId, String locationId) {
        boolean hasConsumptionProfile = consumptionProfileRepository.existsByProductIdAndLocationId(productId, locationId);
        boolean hasLeadTime = leadTimeRepository.existsByProductIdAndLocationIdAndIsActive(productId, locationId, true);

        return hasConsumptionProfile && hasLeadTime;
    }

    private Double calculateRLT(LeadTime leadTime) {
        if (leadTime == null) {
            return null;
        }

        Double rlt = 0.0;

        if (leadTime.getManufacturingTime() != null) {
            rlt += leadTime.getManufacturingTime();
        }

        if (leadTime.getOrderLeadTime() != null) {
            rlt += leadTime.getOrderLeadTime();
        }

        if (leadTime.getTransportTime() != null) {
            rlt += leadTime.getTransportTime();
        }

        return rlt > 0 ? rlt : null;
    }

    private Double getBaseADCValue(ConsumptionProfile profile, String baseADC) {
        if (profile == null) {
            return null;
        }

        switch (baseADC.toLowerCase()) {
            case "7adc":
                return profile.getAdc7d();
            case "14adc":
                return profile.getAdc14d();
            case "30adc":
                return profile.getAdc30d();
            default:
                return null;
        }
    }

    private BufferCalculationResponseDTO performCalculation(BufferCalculationRequestDTO request,
                                                            ConsumptionProfile profile,
                                                            Double rlt,
                                                            Double baseADCValue) {
        // Calculate buffer units
        Double bufferUnits = baseADCValue * rlt;

        // Calculate safety buffer units
        Double safetyBufferUnits = bufferUnits * (request.getSafetyFactor() / 100.0);

        // Calculate final quantity
        Double finalQuantity = bufferUnits + safetyBufferUnits;

        return BufferCalculationResponseDTO.builder()
                .productId(request.getProductId())
                .locationId(request.getLocationId())
                .rlt(rlt)
                .adc7d(profile != null ? profile.getAdc7d() : null)
                .adc14d(profile != null ? profile.getAdc14d() : null)
                .adc30d(profile != null ? profile.getAdc30d() : null)
                .baseADC(request.getBaseADC())
                .safetyFactor(request.getSafetyFactor())
                .bufferUnits(Math.round(bufferUnits * 100.0) / 100.0) // Round to 2 decimal places
                .safetyBufferUnits(Math.round(safetyBufferUnits * 100.0) / 100.0)
                .finalQuantity(Math.round(finalQuantity * 100.0) / 100.0)
                .calculationStatus("SUCCESS")
                .message("Buffer calculation completed successfully")
                .build();
    }

    private BufferCalculationResponseDTO createPartialDataResponse(BufferCalculationRequestDTO request,
                                                                   ConsumptionProfile profile,
                                                                   Double rlt,
                                                                   String message) {
        return BufferCalculationResponseDTO.builder()
                .productId(request.getProductId())
                .locationId(request.getLocationId())
                .rlt(rlt)
                .adc7d(profile != null ? profile.getAdc7d() : null)
                .adc14d(profile != null ? profile.getAdc14d() : null)
                .adc30d(profile != null ? profile.getAdc30d() : null)
                .baseADC(request.getBaseADC())
                .safetyFactor(request.getSafetyFactor())
                .bufferUnits(null)
                .safetyBufferUnits(null)
                .finalQuantity(null)
                .calculationStatus("PARTIAL_DATA")
                .message(message)
                .build();
    }

    private BufferCalculationResponseDTO createErrorResponse(BufferCalculationRequestDTO request, String message) {
        return BufferCalculationResponseDTO.builder()
                .productId(request.getProductId())
                .locationId(request.getLocationId())
                .baseADC(request.getBaseADC())
                .safetyFactor(request.getSafetyFactor())
                .calculationStatus("ERROR")
                .message(message)
                .build();
    }
}
