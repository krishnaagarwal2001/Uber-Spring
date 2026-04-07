package com.example.Uber.configurations;

import com.example.Uber.services.implementations.RideService;
import io.grpc.ServerBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import io.grpc.Server;

import java.io.IOException;

import jakarta.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class GrpcServerConfig {

    @Value("${grpc.server.port}")
    private int grpcServerPort;

    private final RideService rideService;
    private Server server;

    /*
        Runs automatically after Spring initializes this bean
        So gRPC server starts when the app starts
    */
    @PostConstruct
    public void startGrpcServer() throws IOException {
        server = ServerBuilder
                .forPort(grpcServerPort) //Creates a gRPC server on given port
                .addService(rideService) // Registers service implementation. service must implement RideServiceGrpc.RideServiceImplBase
                .build() //Prepares the server
                .start(); //Starts listening for incoming gRPC requests

        System.out.println("gRPC Server started on port " + grpcServerPort);

        /*
            Keep server alive in background

            gRPC server runs asynchronously
            If we don’t block → app may exit

            New thread, So it doesn’t block Spring main thread
        */
        new Thread(() -> {
            try {
                if( server != null ) {
                    server.awaitTermination(); //Blocks thread until server shuts down. Keeps server running
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("gRPC Server interrupted");
            }
        }).start();

        /*
            Runs when app is shutting down (Ctrl+C / kill / crash)
        */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gRPC Server...");
            if( server != null ) {
                server.shutdown();
            }
        }));
    }
}
