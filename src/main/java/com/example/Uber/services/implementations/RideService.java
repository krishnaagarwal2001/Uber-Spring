package com.example.Uber.services.implementations;

import com.example.Uber.RideAcceptanceRequest;
import com.example.Uber.RideAcceptanceResponse;
import com.example.Uber.RideServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RideService extends RideServiceGrpc.RideServiceImplBase {
    private final BookingService bookingService;

    @Override
    public void acceptRide(RideAcceptanceRequest rideAcceptanceRequest, StreamObserver<RideAcceptanceResponse> responseObserver){

    }
}
