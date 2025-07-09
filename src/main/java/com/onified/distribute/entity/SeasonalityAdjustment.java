package com.onified.distribute.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "seasonality_adjustment")
@CompoundIndexes({
        @CompoundIndex(name = "productId_locationId_month_isActive_year",
                def = "{'productId': 1, 'locationId': 1, 'month': 1, 'isActive': 1, 'year': 1}"),
        @CompoundIndex(name = "type_locationId_isActive_year",
                def = "{'type': 1, 'locationId': 1, 'isActive': 1, 'year': 1}"),
        @CompoundIndex(name = "category_isActive_year",
                def = "{'category': 1, 'isActive': 1, 'year': 1}"),
        @CompoundIndex(name = "year_month_isActive",
                def = "{'year': -1, 'month': 1, 'isActive': 1}"),
        @CompoundIndex(name = "productId_category_text_search",
                def = "{'productId': 'text', 'category': 'text', 'description': 'text'}"),
        @CompoundIndex(name = "adjustmentId_unique",
                def = "{'adjustmentId': 1}", unique = true)
})
public class SeasonalityAdjustment {
    @Id
    private String id;

    @Indexed(unique = true)
    private String adjustmentId;

    @Indexed
    private String productId;

    @Indexed
    private String locationId;

    @Indexed
    private String category;

    @Indexed
    private Integer month;

    private Double seasonalityFactor;

    @Indexed
    private Integer year;

    private String description;

    private String confidenceLevel;

    @Indexed
    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;

    @Indexed
    private String type; // SKU, Category, Subcategory, ALL
}