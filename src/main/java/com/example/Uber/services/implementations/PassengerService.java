package com.example.Uber.services.implementations;

import com.example.Uber.adapters.PassengerAdapter;
import com.example.Uber.dtos.PassengerRequest;
import com.example.Uber.dtos.PassengerResponse;
import com.example.Uber.entities.Passenger;
import com.example.Uber.repositories.PassengerRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PassengerService implements com.example.Uber.services.interfaces.PassengerService {

    private final PassengerRepository passengerRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<PassengerResponse> findByEmail(String email) {
        return passengerRepository.findByEmail(email)
                .map(PassengerAdapter::passengerToPassengerResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PassengerResponse> findById(Long id) {
        return passengerRepository.findById(id)
                .map(PassengerAdapter::passengerToPassengerResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PassengerResponse> findAll() {
        return passengerRepository.findAll()
                .stream()
                .map(PassengerAdapter::passengerToPassengerResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PassengerResponse create(PassengerRequest passengerRequest) {
        if(passengerRepository.existsByEmail(passengerRequest.getEmail())) {
            throw new IllegalArgumentException("Passenger with email " + passengerRequest.getEmail() + " already exists");
        }

        Passenger passenger = PassengerAdapter.passengerRequestToPassenger(passengerRequest);
        Passenger savedPassenger = passengerRepository.save(passenger);
        return PassengerAdapter.passengerToPassengerResponse(savedPassenger);
    }

    @Override
    public PassengerResponse update(Long id, PassengerRequest passengerRequest) {
        Passenger passenger = passengerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found with id: " + id));

        if (!passenger.getEmail().equals(passengerRequest.getEmail()) &&
                passengerRepository.existsByEmail(passengerRequest.getEmail())) {
            throw new IllegalArgumentException("Passenger with email " + passengerRequest.getEmail() + " already exists");
        }

        PassengerAdapter.updateEntity(passenger, passengerRequest);
        Passenger savedPassenger = passengerRepository.save(passenger);
        return PassengerAdapter.passengerToPassengerResponse(savedPassenger);
    }

    @Override
    public void deleteById(Long id) {
        if (!passengerRepository.existsById(id)) {
            throw new IllegalArgumentException("Passenger not found with id: " + id);
        }
        passengerRepository.deleteById(id);
    }
}
