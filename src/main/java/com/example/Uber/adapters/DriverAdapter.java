package com.example.Uber.adapters;

import com.example.Uber.dtos.DriverRequest;
import com.example.Uber.dtos.DriverResponse;
import com.example.Uber.entities.Driver;

public class DriverAdapter {
    public static Driver driverRequestToDriver(DriverRequest driverRequest){
        return Driver.builder()
                .name(driverRequest.getName())
                .email(driverRequest.getEmail())
                .phoneNumber(driverRequest.getPhoneNumber())
                .licenseNumber(driverRequest.getLicenseNumber())
                .vehicleModel(driverRequest.getVehicleModel())
                .vehiclePlateNumber(driverRequest.getVehiclePlateNumber())
                .isAvailable(
                        driverRequest.getIsAvailable() != null
                                ? driverRequest.getIsAvailable()
                                : true
                )
                .build();
    }

    public static DriverResponse driverToDriverResponse(Driver driver){
        return DriverResponse.builder()
                .id(driver.getId())
                .name(driver.getName())
                .email(driver.getEmail())
                .phoneNumber(driver.getPhoneNumber())
                .licenseNumber(driver.getLicenseNumber())
                .vehicleModel(driver.getVehicleModel())
                .vehiclePlateNumber(driver.getVehiclePlateNumber())
                .isAvailable(driver.getIsAvailable())
                .createdAt(driver.getCreatedAt())
                .updatedAt(driver.getUpdatedAt())
                .build();
    }

    public static void updateEntity(DriverRequest driverRequest, Driver driver){
        driver.setName(driverRequest.getName());
        driver.setEmail(driverRequest.getEmail());
        driver.setPhoneNumber(driverRequest.getPhoneNumber());
        driver.setLicenseNumber(driverRequest.getLicenseNumber());
        driver.setVehicleModel(driverRequest.getVehicleModel());
        driver.setVehiclePlateNumber(driverRequest.getVehiclePlateNumber());

        if (driverRequest.getIsAvailable() != null) {
            driver.setIsAvailable(driverRequest.getIsAvailable());
        }
    }
}
