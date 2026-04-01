package com.example.Uber.services.implementations;

import com.example.Uber.dtos.DriverLocationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService implements com.example.Uber.services.interfaces.LocationService {
    @Value("${redis.geo.ops.key}")
    private String driverGeoOpsKey;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Boolean saveDriverLocation(String driverId, Double latitude, Double longitude) {
        GeoOperations<String, String> geoOperations = stringRedisTemplate.opsForGeo();

        geoOperations.add(driverGeoOpsKey,
                new RedisGeoCommands.GeoLocation<>(driverId, new Point(latitude, longitude))
        );

        return true;
    }

    @Override
    public List<DriverLocationDTO> getNearbyDrivers(Double latitude, Double longitude, Double radius) {
        GeoOperations<String, String> geoOperations = stringRedisTemplate.opsForGeo();

        Distance circleRadius = new Distance(radius, Metrics.KILOMETERS);
        Circle circle = new Circle(new Point(latitude, longitude), circleRadius);

        GeoResults<RedisGeoCommands.GeoLocation<String>> results = geoOperations.radius(driverGeoOpsKey, circle);

        List<DriverLocationDTO> driverLocations = new ArrayList<>();

        for(GeoResult<RedisGeoCommands.GeoLocation<String>> result : results) {

            Point point = geoOperations.position(driverGeoOpsKey, result.getContent().getName()).get(0);

            DriverLocationDTO driverLocation = DriverLocationDTO.builder()
                    .driverId(result.getContent().getName())
                    .latitude(point.getY())
                    .longitude(point.getX())
                    .build();

            driverLocations.add(driverLocation);
        }

        return driverLocations;
    }
}
