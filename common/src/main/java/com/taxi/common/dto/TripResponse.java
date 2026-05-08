package com.taxi.common.dto;

import com.taxi.common.enums.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripResponse {
    private Long id;
    private Long passengerId;
    private Long driverId;
    private String origin;
    private double originLat;
    private double originLng;
    private String destination;
    private double destLat;
    private double destLng;
    private TripStatus status;
    private BigDecimal price;
    private Integer rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
