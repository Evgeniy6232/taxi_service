package com.taxi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripCreateRequest {
    private String origin;
    private double originLat;
    private double originLng;
    private String destination;
    private double destLat;
    private double destLng;
}
