package com.taxi.trip.dto;

import com.taxi.common.enums.TripStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTripStatusRequest {
    @NotNull
    private TripStatus status;
}
