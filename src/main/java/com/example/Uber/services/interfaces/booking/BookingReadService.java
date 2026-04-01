package com.example.Uber.services.interfaces.booking;

import com.example.Uber.dtos.BookingResponse;
import com.example.Uber.services.interfaces.ReadService;

import java.util.List;

public interface BookingReadService extends ReadService<BookingResponse, Long> {
    List<BookingResponse> findByPassengerId(Long passengerId);
    List<BookingResponse> findByDriverId(Long driverId);
}
