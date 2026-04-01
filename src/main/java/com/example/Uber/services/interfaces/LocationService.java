package com.example.Uber.services.interfaces;

import com.example.Uber.dtos.DriverLocationDTO;

import java.util.List;

public interface LocationService {
    Boolean saveDriverLocation(String driverId, Double latitude, Double longitude);

    List<DriverLocationDTO> getNearbyDrivers(Double latitude, Double longitude, Double radius);
}
