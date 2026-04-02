package com.example.Uber.adapters;

import com.example.Uber.dtos.BookingRequest;
import com.example.Uber.dtos.BookingResponse;
import com.example.Uber.entities.Booking;
import com.example.Uber.entities.Driver;
import com.example.Uber.entities.Passenger;
import com.example.Uber.enums.BookingStatus;

public class BookingAdapter {
    public static Booking bookingRequestToBooking(BookingRequest bookingRequest, Passenger passenger, Driver driver){
        BookingStatus bookingStatus = driver != null ? BookingStatus.CONFIRMED : BookingStatus.PENDING;

        return Booking.builder()
                .passenger(passenger)
                .driver(driver)
                .pickupLocationLatitude(bookingRequest.getPickupLocationLatitude())
                .pickupLocationLongitude(bookingRequest.getPickupLocationLongitude())
                .dropoffLocation(bookingRequest.getDropoffLocation())
                .status(bookingStatus)
                .fare(bookingRequest.getFare())
                .scheduledPickupTime(bookingRequest.getScheduledPickupTime())
                .build();
    }

    public static BookingResponse bookingToBookingResponse(Booking booking){
        return BookingResponse.builder()
                .id(booking.getId())
                .passengerId(booking.getPassenger().getId())
                .passengerName(booking.getPassenger().getName())
                .driverId(booking.getDriver().getId())
                .driverName(booking.getDriver().getName())
                .pickupLocationLatitude(booking.getPickupLocationLatitude())
                .pickupLocationLongitude(booking.getPickupLocationLongitude())
                .dropoffLocation(booking.getDropoffLocation())
                .fare(booking.getFare())
                .scheduledPickupTime(booking.getScheduledPickupTime())
                .status(booking.getStatus())
                .actualPickupTime(booking.getActualPickupTime())
                .completedAt(booking.getCompletedAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    public static void updateEntity(BookingRequest request, Booking booking, Passenger passenger, Driver driver){
        booking.setPassenger(passenger);
        booking.setDriver(driver);
        booking.setPickupLocationLatitude(request.getPickupLocationLatitude());
        booking.setPickupLocationLongitude(request.getPickupLocationLongitude());
        booking.setDropoffLocation(request.getDropoffLocation());
        booking.setFare(request.getFare());
        booking.setScheduledPickupTime(request.getScheduledPickupTime());

        if (driver != null && booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CONFIRMED);
        }
    }
}
