package com.taxi.trip.service;

import com.taxi.common.util.DistanceCalculator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PricingService {

    private final double tariffPerKm;

    public PricingService(@Value("${taxi.tariff-per-km}") double tariffPerKm) {
        this.tariffPerKm = tariffPerKm;
    }

    public BigDecimal calculatePrice(double lat1, double lng1, double lat2, double lng2) {
        double distance = DistanceCalculator.calculate(lat1, lng1, lat2, lng2);
        return BigDecimal.valueOf(distance * tariffPerKm)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
