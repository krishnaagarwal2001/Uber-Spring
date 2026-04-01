package com.example.Uber.adapters;

import com.example.Uber.dtos.PassengerRequest;
import com.example.Uber.dtos.PassengerResponse;
import com.example.Uber.entities.Passenger;

public class PassengerAdapter {
    public static Passenger passengerRequestToPassenger(PassengerRequest passengerRequest) {
        return Passenger.builder()
                .name(passengerRequest.getName())
                .email(passengerRequest.getEmail())
                .phoneNumber(passengerRequest.getPhoneNumber())
                .build();
    }

    public static PassengerResponse passengerToPassengerResponse(Passenger passenger) {
        return PassengerResponse.builder()
                .id(passenger.getId())
                .name(passenger.getName())
                .email(passenger.getEmail())
                .phoneNumber(passenger.getPhoneNumber())
                .createdAt(passenger.getCreatedAt())
                .updatedAt(passenger.getUpdatedAt())
                .build();
    }

    public static void updateEntity(Passenger passenger, PassengerRequest passengerRequest) {
        passenger.setName(passengerRequest.getName());
        passenger.setEmail(passengerRequest.getEmail());
        passenger.setPhoneNumber(passengerRequest.getPhoneNumber());
    }
}
