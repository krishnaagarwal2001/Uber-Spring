# gRPC Integration in Uber-Spring

This document explains how gRPC is integrated into the Uber-Spring application, including the setup, configuration, and usage patterns.

## Overview

The application uses gRPC for real-time communication between services, specifically for ride acceptance and driver notifications. gRPC provides efficient, type-safe communication using Protocol Buffers.

## Protocol Buffer Definition

The gRPC services are defined in `src/main/proto/Ride.proto`:

```protobuf
syntax = "proto3";

package com.example.Uber;

option java_multiple_files = true;
option java_package = "com.example.Uber";
option java_outer_classname = "Ride";

message RideAcceptanceRequest {
  int64 driver_id = 1;
  int64 booking_id = 2;
}

message RideAcceptanceResponse {
  bool success = 1;
}

service RideService {
  rpc acceptRide(RideAcceptanceRequest) returns (RideAcceptanceResponse);
}

message RideNotificationRequest {
  double pick_up_location_latitude = 1;
  double pick_up_location_longitude = 2;
  int64 booking_id = 3;
  repeated int64 driver_ids = 4;
}

message RideNotificationResponse {
  bool success = 1;
}

service RideNotificationService {
  rpc notifyDriversForNewRide(RideNotificationRequest) returns (RideNotificationResponse);
}
```

### Services Defined

1. **RideService**: Handles ride acceptance by drivers
   - `acceptRide`: Allows a driver to accept a pending booking

2. **RideNotificationService**: Handles notifications to drivers
   - `notifyDriversForNewRide`: Notifies nearby drivers about a new ride request

## Build Configuration

The `build.gradle` file includes the necessary dependencies and plugins for gRPC:

```gradle
plugins {
    id 'com.google.protobuf' version '0.9.5'
}

dependencies {
    // Protobuf
    implementation 'com.google.protobuf:protobuf-java:3.25.5'

    // gRPC
    implementation 'io.grpc:grpc-protobuf:1.60.1'
    implementation 'io.grpc:grpc-stub:1.60.1'
    implementation 'io.grpc:grpc-netty-shaded:1.60.1'
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"
    }
    plugins {
        grpc {
            artifact = "io.grpc:protoc-gen-grpc-java:1.60.1"
        }
    }
    generateProtoTasks {
        all()*.plugins {
            grpc {}
        }
    }
}

sourceSets {
    main {
        proto {
            srcDir 'src/main/proto'
        }
    }
}
```

The protobuf plugin automatically generates Java classes from the `.proto` files during the build process.

## Server Configuration

The gRPC server is configured in `GrpcServerConfig.java`:

```java
@Configuration
@RequiredArgsConstructor
public class GrpcServerConfig {

    @Value("${grpc.server.port}")
    private int grpcServerPort;

    private final RideService rideService;
    private Server server;

    @PostConstruct
    public void startGrpcServer() throws IOException {
        server = ServerBuilder
                .forPort(grpcServerPort)
                .addService(rideService)
                .build()
                .start();

        // Server runs in background thread
        new Thread(() -> {
            try {
                server.awaitTermination();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
```

- The server starts automatically when the Spring application context initializes
- Runs on the port specified by `grpc.server.port` (default: 9090)
- Registers the `RideService` implementation
- Runs in a background thread to avoid blocking the main Spring application

## Client Configuration

The gRPC client is configured in `GrpcClient.java`:

```java
@Component
public class GrpcClient {

    @Value("${grpc.client.port:9091}")
    private int grpcClientPort;

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
        // Implementation details...
    }
}
```

- Creates a managed channel to the gRPC server
- Uses plaintext communication (no TLS)
- Creates blocking stubs for synchronous calls
- Connects to `grpc.client.host:grpc.client.port` (default: localhost:9091)

## Service Implementation

The `RideService` implementation extends the generated gRPC base class:

```java
@Service
@RequiredArgsConstructor
public class RideService extends RideServiceGrpc.RideServiceImplBase {
    private final BookingService bookingService;

    @Override
    public void acceptRide(RideAcceptanceRequest request,
            StreamObserver<RideAcceptanceResponse> responseObserver) {
        Boolean success = bookingService.acceptRide(request.getDriverId(), request.getBookingId());

        RideAcceptanceResponse response = RideAcceptanceResponse.newBuilder()
                .setSuccess(success)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
```

- Extends `RideServiceGrpc.RideServiceImplBase`
- Delegates business logic to the `BookingService`
- Uses `StreamObserver` for asynchronous response handling

## Usage in Business Logic

### Ride Creation Flow

When a new booking is created in `BookingService.create()`:

1. The booking is saved to the database
2. Nearby drivers are retrieved using the location service
3. The gRPC client is called to notify drivers:

```java
List<DriverLocationDTO> nearByDrivers = locationService.getNearbyDrivers(
        bookingRequest.getPickupLocationLatitude(),
        bookingRequest.getPickupLocationLongitude(),
        10.0);

List<Long> driverIds = nearByDrivers.stream()
        .map(DriverLocationDTO::getDriverId)
        .collect(Collectors.toList());

grpcClient.notifyDriversForNewRide(
        bookingRequest.getPickupLocationLatitude(),
        bookingRequest.getPickupLocationLongitude(),
        bookingId,
        driverIds);
```

### Ride Acceptance Flow

When a driver accepts a ride:

1. The gRPC `acceptRide` method is called
2. This invokes `BookingService.acceptRide()` which:
   - Validates the booking and driver
   - Updates the booking status to CONFIRMED
   - Assigns the driver to the booking
   - Marks the driver as unavailable

## Configuration Properties

The following properties control gRPC behavior (defined in `application.properties`):

```properties
grpc.server.port=${GRPC_SERVER_PORT:9090}
grpc.client.port=${GRPC_CLIENT_PORT:9091}
grpc.client.host=${GRPC_CLIENT_HOST}
```

## Error Handling

The implementation uses gRPC status codes for error handling:

- `Status.NOT_FOUND`: When booking or driver is not found
- `Status.FAILED_PRECONDITION`: When booking is not available for acceptance or driver is already assigned/unavailable

## Architecture Notes

- **Separation of Concerns**: gRPC handles communication, while business logic remains in Spring services
- **Asynchronous Communication**: The server runs in a background thread, allowing non-blocking operation
- **Type Safety**: Protocol Buffers ensure type-safe communication between services
- **Scalability**: gRPC's efficient binary protocol supports high-throughput communication

## Future Enhancements

Potential improvements could include:
- Adding TLS encryption for production deployments
- Implementing streaming RPCs for real-time updates
- Adding authentication and authorization
- Implementing load balancing for multiple gRPC servers</content>
<parameter name="filePath">/workspaces/Uber-Spring/GRPC_INTEGRATION.md