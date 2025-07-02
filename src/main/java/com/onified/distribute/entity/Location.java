package com.onified.distribute.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "locations")
@CompoundIndex(name = "region_isActive", def = "{'region': 1, 'isActive': 1}")
@CompoundIndex(name = "type_isActive", def = "{'type': 1, 'isActive': 1}")
public class Location {
    @Id
    private String id;
    @Indexed(unique = true)
    private String locationId;
    private String code;
    private String name;
    @Indexed
    private String type;
    private String region;
    private String parentLocationId;
    private Address address;
    @Indexed
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class Address {
        private String street;
        private String city;
        private String state;
        private String country;
        private String postalCode;
    }
}
