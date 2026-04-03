package com.example.Uber.client;

import com.example.Uber.RideNotificationRequest;
import com.example.Uber.RideNotificationResponse;
import com.example.Uber.RideNotificationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class GrpcClient {

    @Value("${grpc.client.port:9091}")
    private Integer grpcClientPort;

    @Value("${grpc.client.host:localhost}")
    private String grpcClientHost;

    private ManagedChannel channel;

    private RideNotificationServiceGrpc.RideNotificationServiceBlockingStub rideNotificationServiceStub;

    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress(grpcClientHost, grpcClientPort)
                .usePlaintext()
                .build();

        rideNotificationServiceStub = RideNotificationServiceGrpc.newBlockingStub(channel);
    }

    public boolean notifyDriversForNewRide(Double pickupLocationLatitude,
            Double pickupLocationLongitude,
            Long bookingId,
            List<Long> driverIds) {

        RideNotificationRequest request = RideNotificationRequest.newBuilder()
                .setPickUpLocationLatitude(pickupLocationLatitude)
                .setPickUpLocationLongitude(pickupLocationLongitude)
                .setBookingId(bookingId)
                .addAllDriverIds(driverIds)
                .build();

        RideNotificationResponse response = rideNotificationServiceStub.notifyDriversForNewRide(request);

        return response.getSuccess();
    }
}
