package com.example.Uber.services.interfaces;

import com.example.Uber.dtos.DriverRequest;
import com.example.Uber.dtos.DriverResponse;
import com.example.Uber.entities.Driver;

import java.util.List;
import java.util.Optional;

public interface DriverService extends ReadService<DriverResponse,Long> , WriteService<DriverRequest, DriverResponse, Long> {
    Optional<DriverResponse> findByEmail(String email);

    List<DriverResponse> findAvailableDrivers();
}
