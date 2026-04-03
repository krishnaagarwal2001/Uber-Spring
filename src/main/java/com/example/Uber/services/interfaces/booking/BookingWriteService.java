package com.example.Uber.services.interfaces.booking;

import com.example.Uber.dtos.BookingRequest;
import com.example.Uber.dtos.BookingResponse;
import com.example.Uber.enums.BookingStatus;
import com.example.Uber.services.interfaces.WriteService;

public interface BookingWriteService extends WriteService<BookingRequest, BookingResponse, Long> {
    BookingResponse updateStatus(Long id, BookingStatus bookingStatus);

    Boolean acceptRide(Long driverId, Long bookingId);
}
