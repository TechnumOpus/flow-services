package com.onified.distribute.dto;

import lombok.Data;
import lombok.*;

import java.time.LocalDateTime;

@Data
public class LocationDTO {
    private String id;
    private String locationId;
    private String name;
    private String region;
    private AddressDTO address;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    public static class AddressDTO {
        private String street;
        private String city;
        private String state;
        private String country;
        private String postalCode;
    }
}
