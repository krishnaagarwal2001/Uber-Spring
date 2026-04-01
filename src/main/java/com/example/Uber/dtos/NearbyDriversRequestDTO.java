package com.example.Uber.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyDriversRequestDTO {
    private Double latitude;
    private Double longitude;
    private Double radius;
}
