package com.onified.distribute.service;

import com.onified.distribute.dto.LocationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LocationService {
    LocationDTO createLocation(LocationDTO locationDto);
    LocationDTO updateLocation(String locationId, LocationDTO locationDto);
    LocationDTO getLocationById(String locationId);
    Page<LocationDTO> getAllLocations(Pageable pageable);
    Page<LocationDTO> getActiveLocations(Pageable pageable);
    Page<LocationDTO> getLocationsByType(String type, Pageable pageable);
    List<LocationDTO> getLocationsByIds(List<String> locationIds);
    LocationDTO activateLocation(String locationId);
    LocationDTO deactivateLocation(String locationId);
    void deleteLocation(String locationId);
    boolean existsByLocationId(String locationId);
    Page<LocationDTO> getLocationsByRegion(String region, Pageable pageable);
    Page<LocationDTO> getLocationsByRegionIgnoreCase(String region, Pageable pageable);

}