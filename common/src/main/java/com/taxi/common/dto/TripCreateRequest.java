package com.taxi.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripCreateRequest {
    @NotBlank
    private String origin;
    @NotNull
    private Double originLat;
    @NotNull
    private Double originLng;
    @NotBlank
    private String destination;
    @NotNull
    private Double destLat;
    @NotNull
    private Double destLng;
}
