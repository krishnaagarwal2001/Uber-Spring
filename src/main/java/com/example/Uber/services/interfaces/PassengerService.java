package com.example.Uber.services.interfaces;

import com.example.Uber.dtos.PassengerRequest;
import com.example.Uber.dtos.PassengerResponse;
import com.example.Uber.entities.Passenger;

import java.util.Optional;

public interface PassengerService extends ReadService<PassengerResponse, Long>, WriteService<PassengerRequest, PassengerResponse, Long> {
    Optional<PassengerResponse> findByEmail(String email);
}
