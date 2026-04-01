package com.example.Uber.repositories;

import com.example.Uber.entities.Booking;
import com.example.Uber.entities.Driver;
import com.example.Uber.entities.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByPassenger(Passenger passenger);

    List<Booking> findByDriver(Driver driver);
}
