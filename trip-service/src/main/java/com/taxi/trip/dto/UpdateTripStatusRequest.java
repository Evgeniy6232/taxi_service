package com.taxi.trip.dto;

import com.taxi.common.enums.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTripStatusRequest {
    private TripStatus status;
}
