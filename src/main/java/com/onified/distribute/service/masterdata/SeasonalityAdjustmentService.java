package com.onified.distribute.service.masterdata;

import com.onified.distribute.dto.SeasonalityAdjustmentDTO;
import com.onified.distribute.dto.SeasonalityMatrixResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SeasonalityAdjustmentService {

    SeasonalityAdjustmentDTO createSeasonalityAdjustment(SeasonalityAdjustmentDTO adjustmentDto);

    SeasonalityAdjustmentDTO getSeasonalityAdjustmentById(String adjustmentId);

    Page<SeasonalityAdjustmentDTO> getAllSeasonalityAdjustments(Pageable pageable);

    Page<SeasonalityAdjustmentDTO> getActiveSeasonalityAdjustments(Pageable pageable);

    Page<SeasonalityAdjustmentDTO> getSeasonalityAdjustmentsByProduct(String productId, Pageable pageable);

    Page<SeasonalityAdjustmentDTO> getSeasonalityAdjustmentsByLocation(String locationId, Pageable pageable);

    Page<SeasonalityAdjustmentDTO> getSeasonalityAdjustmentsByMonth(Integer month, Pageable pageable);

    List<SeasonalityAdjustmentDTO> getSeasonalityAdjustmentsByProductAndLocation(String productId, String locationId);

    Double getSeasonalityFactor(String productId, String locationId, Integer month);

    SeasonalityAdjustmentDTO activateSeasonalityAdjustment(String adjustmentId);

    SeasonalityAdjustmentDTO deactivateSeasonalityAdjustment(String adjustmentId);

    void deleteSeasonalityAdjustment(String adjustmentId);

    SeasonalityAdjustmentDTO updateSeasonalityAdjustment(String adjustmentId, SeasonalityAdjustmentDTO adjustmentDto);

    Page<SeasonalityMatrixResponseDTO> getSeasonalityMatrix(String type, String locationId, String category,
                                                            String productId, Integer year, Boolean isActive, Pageable pageable);

    List<SeasonalityAdjustmentDTO> updateSeasonalityBulk(List<SeasonalityAdjustmentDTO> adjustments);
}
