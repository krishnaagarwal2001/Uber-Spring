package com.example.Uber.services.implementations;

import com.example.Uber.adapters.DriverAdapter;
import com.example.Uber.dtos.DriverRequest;
import com.example.Uber.dtos.DriverResponse;
import com.example.Uber.entities.Driver;
import com.example.Uber.repositories.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DriverService implements com.example.Uber.services.interfaces.DriverService {
    private final DriverRepository driverRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<DriverResponse> findByEmail(String email) {
        return driverRepository.findByEmail(email)
                .map(DriverAdapter::driverToDriverResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverResponse> findAvailableDrivers() {
        return driverRepository.findAll().stream()
                .filter(Driver::getIsAvailable)
                .map(DriverAdapter::driverToDriverResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DriverResponse> findById(Long id) {
        return driverRepository.findById(id)
                .map(DriverAdapter::driverToDriverResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverResponse> findAll() {
        return driverRepository.findAll()
                .stream()
                .map(DriverAdapter::driverToDriverResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DriverResponse create(DriverRequest driverRequest) {
        if (driverRepository.existsByEmail(driverRequest.getEmail())) {
            throw new IllegalArgumentException("Driver with email " + driverRequest.getEmail() + " already exists");
        }

        if (driverRepository.existsByLicenseNumber(driverRequest.getLicenseNumber())) {
            throw new IllegalArgumentException("Driver with license number " + driverRequest.getLicenseNumber() + " already exists");
        }

        Driver driver = DriverAdapter.driverRequestToDriver(driverRequest);
        Driver savedDriver = driverRepository.save(driver);
        return DriverAdapter.driverToDriverResponse(savedDriver);
    }

    @Override
    public DriverResponse update(Long id, DriverRequest driverRequest) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with id: " + id));

        // Check if email is being changed and if new email already exists
        if (!driver.getEmail().equals(driverRequest.getEmail()) &&
                driverRepository.existsByEmail(driverRequest.getEmail())) {
            throw new IllegalArgumentException("Driver with email " + driverRequest.getEmail() + " already exists");
        }

        // Check if license number is being changed and if new license already exists
        if (!driver.getLicenseNumber().equals(driverRequest.getLicenseNumber()) &&
                driverRepository.existsByLicenseNumber(driverRequest.getLicenseNumber())) {
            throw new IllegalArgumentException("Driver with license number " + driverRequest.getLicenseNumber() + " already exists");
        }

        DriverAdapter.updateEntity(driverRequest, driver);
        Driver updatedDriver = driverRepository.save(driver);
        return DriverAdapter.driverToDriverResponse(updatedDriver);
    }

    @Override
    public void deleteById(Long id) {
        if (!driverRepository.existsById(id)) {
            throw new IllegalArgumentException("Driver not found with id: " + id);
        }
        driverRepository.deleteById(id);
    }
}
