package com.example.Uber.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverRequest {

    @NotNull(message = "Name is required")
    private String name;

    @NotNull(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotNull(message = "Phone number is required")
    private String phoneNumber;

    @NotNull(message = "License Number is required")
    private String licenseNumber;

    private String vehicleModel;

    private String vehiclePlateNumber;

    @NotNull
    @Builder.Default
    private Boolean isAvailable = true;
}
