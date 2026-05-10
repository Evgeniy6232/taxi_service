package com.taxi.user.dto;

import com.taxi.common.enums.DriverStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDriverStatusRequest {
    @NotNull
    private DriverStatus status;
}
