package com.example.Uber.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotNull(message = "Passenger ID is required")
    private Long passengerId;

    private Long driverId;

    @NotNull(message = "Pickup location latitude is required")
    private Double pickupLocationLatitude;

    @NotNull(message = "Pickup location longitude is required")
    private Double pickupLocationLongitude;

    private String dropoffLocation;

    private BigDecimal fare;

    private LocalDateTime scheduledPickupTime;
}
