package com.example.Uber.services.implementations;

import com.example.Uber.adapters.BookingAdapter;
import com.example.Uber.client.GrpcClient;
import com.example.Uber.dtos.BookingRequest;
import com.example.Uber.dtos.BookingResponse;
import com.example.Uber.dtos.DriverLocationDTO;
import com.example.Uber.entities.Booking;
import com.example.Uber.entities.Driver;
import com.example.Uber.entities.Passenger;
import com.example.Uber.enums.BookingStatus;
import com.example.Uber.repositories.BookingRepository;
import com.example.Uber.repositories.DriverRepository;
import com.example.Uber.repositories.PassengerRepository;
import com.example.Uber.services.interfaces.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingService implements com.example.Uber.services.interfaces.booking.BookingService {

    private final BookingRepository bookingRepository;
    private final DriverRepository driverRepository;
    private final PassengerRepository passengerRepository;
    private final LocationService locationService;

    private final GrpcClient grpcClient;

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> findByPassengerId(Long passengerId) {
        Passenger passenger = passengerRepository.findById(passengerId)
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found with id: " + passengerId));

        return bookingRepository.findByPassenger(passenger).stream()
                .map(BookingAdapter::bookingToBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> findByDriverId(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found with id: " + driverId));

        return bookingRepository.findByDriver(driver).stream()
                .map(BookingAdapter::bookingToBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BookingResponse> findById(Long id) {
        return bookingRepository.findById(id)
                .map(BookingAdapter::bookingToBookingResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> findAll() {
        return bookingRepository.findAll().stream()
                .map(BookingAdapter::bookingToBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponse create(BookingRequest bookingRequest) {
        Passenger passenger = passengerRepository.findById(bookingRequest.getPassengerId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Passenger not found with id: " + bookingRequest.getPassengerId()));

        Driver driver = null;
        BookingStatus status = BookingStatus.PENDING;

        if (bookingRequest.getDriverId() != null) {
            driver = driverRepository.findById(bookingRequest.getDriverId()).orElseThrow(
                    () -> new IllegalArgumentException("Driver not found with id: " + bookingRequest.getDriverId()));

            if (!driver.getIsAvailable()) {
                throw new IllegalStateException("Driver with id " + bookingRequest.getDriverId() + " is not available");
            }

            driver.setIsAvailable(false);
            driverRepository.save(driver);
            status = BookingStatus.CONFIRMED;
        }

        if (bookingRequest.getPickupLocationLatitude() == null || bookingRequest.getPickupLocationLongitude() == null) {
            throw new IllegalArgumentException("Pickup location latitude and longitude are required");
        }

        BigDecimal fare = bookingRequest.getFare();

        if (fare == null) {
            fare = BigDecimal.ZERO; // we can create a microservice for fare calculation
        }

        Booking booking = Booking.builder()
                .passenger(passenger)
                .pickupLocationLatitude(bookingRequest.getPickupLocationLatitude())
                .pickupLocationLongitude(bookingRequest.getPickupLocationLongitude())
                .status(status)
                .driver(driver)
                .fare(fare)
                .dropoffLocation(bookingRequest.getDropoffLocation())
                .scheduledPickupTime(bookingRequest.getScheduledPickupTime())
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        List<DriverLocationDTO> nearByDrivers = locationService.getNearbyDrivers(
                bookingRequest.getPickupLocationLatitude(),
                bookingRequest.getPickupLocationLongitude(),
                10.0);

        List<Long> driverIds = nearByDrivers.stream().map(DriverLocationDTO::getDriverId).collect(Collectors.toList());

        Long bookingId = savedBooking.getId();

        grpcClient.notifyDriversForNewRide(bookingRequest.getPickupLocationLatitude(),
                bookingRequest.getPickupLocationLongitude(), bookingId, driverIds);

        return BookingAdapter.bookingToBookingResponse(savedBooking);
    }

    @Override
    public BookingResponse update(Long id, BookingRequest bookingRequest) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + id));

        Passenger passenger = passengerRepository.findById(bookingRequest.getPassengerId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Passenger not found with id: " + bookingRequest.getPassengerId()));

        Driver driver = null;

        if (bookingRequest.getDriverId() != null) {
            driver = driverRepository.findById(bookingRequest.getDriverId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Driver not found with id: " + bookingRequest.getDriverId()));
        }

        // Handle driver availability when updating
        Driver previousDriver = booking.getDriver();

        if (previousDriver != null && !previousDriver.equals(driver)) {
            previousDriver.setIsAvailable(true);
            driverRepository.save(previousDriver);
        }

        if (driver != null && !driver.equals(previousDriver)) {
            if (!driver.getIsAvailable()) {
                throw new IllegalArgumentException(
                        "Driver with id " + bookingRequest.getDriverId() + " is not available");
            }

            driver.setIsAvailable(false);

            driverRepository.save(driver);
        }

        BookingAdapter.updateEntity(bookingRequest, booking, passenger, driver);
        Booking updatedBooking = bookingRepository.save(booking);
        return BookingAdapter.bookingToBookingResponse(updatedBooking);
    }

    @Override
    public BookingResponse updateStatus(Long id, BookingStatus bookingStatus) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + id));

        booking.setStatus(bookingStatus);

        if (bookingStatus == BookingStatus.IN_PROGRESS && booking.getActualPickupTime() == null) {
            booking.setActualPickupTime(LocalDateTime.now());
        } else if (bookingStatus == BookingStatus.COMPLETED) {
            booking.setCompletedAt(LocalDateTime.now());

            // Release driver
            if (booking.getDriver() != null) {
                Driver driver = booking.getDriver();
                driver.setIsAvailable(true);
                driverRepository.save(driver);
            }
        } else if (bookingStatus == BookingStatus.CANCELLED) {
            // Release driver
            if (booking.getDriver() != null) {
                Driver driver = booking.getDriver();
                driver.setIsAvailable(true);
                driverRepository.save(driver);
            }
        }

        Booking updatedBooking = bookingRepository.save(booking);
        return BookingAdapter.bookingToBookingResponse(updatedBooking);
    }

    @Override
    public void deleteById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + id));

        // Release driver if assigned
        if (booking.getDriver() != null) {
            Driver driver = booking.getDriver();
            driver.setIsAvailable(true);
            driverRepository.save(driver);
        }

        bookingRepository.deleteById(id);
    }
}
