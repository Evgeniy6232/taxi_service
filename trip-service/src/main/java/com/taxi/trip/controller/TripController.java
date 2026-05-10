package com.taxi.trip.controller;

import com.taxi.common.dto.TripCreateRequest;
import com.taxi.common.dto.TripResponse;
import com.taxi.trip.dto.RateTripRequest;
import com.taxi.trip.dto.UpdateTripStatusRequest;
import com.taxi.trip.service.TripService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    public ResponseEntity<TripResponse> createTrip(@Valid @RequestBody TripCreateRequest req,
                                                   Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        Long passengerId = claims.get("refId", Long.class);
        return ResponseEntity.ok(tripService.createTrip(passengerId, req));
    }

    @GetMapping
    public ResponseEntity<List<TripResponse>> getHistory(
            @RequestParam(value = "passenger_id", required = false) Long passengerId,
            Authentication auth) {
        if (passengerId != null) {
            return ResponseEntity.ok(tripService.getByPassenger(passengerId));
        }
        Claims claims = (Claims) auth.getDetails();
        String role = claims.get("role", String.class);
        Long refId = claims.get("refId", Long.class);
        return ResponseEntity.ok(tripService.getTripHistory(role, refId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> getTrip(@PathVariable Long id) {
        return ResponseEntity.ok(tripService.getTrip(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TripResponse> updateStatus(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateTripStatusRequest req,
                                                     Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        String role = claims.get("role", String.class);
        Long refId = claims.get("refId", Long.class);
        return ResponseEntity.ok(tripService.updateStatus(id, req.getStatus(), role, refId));
    }

    @PatchMapping("/{id}/rate")
    public ResponseEntity<TripResponse> rateTrip(@PathVariable Long id,
                                                  @Valid @RequestBody RateTripRequest req,
                                                  Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        String role = claims.get("role", String.class);
        if (!"PASSENGER".equals(role)) {
            throw new IllegalArgumentException("Только пассажир может оценить поездку");
        }
        Long passengerId = claims.get("refId", Long.class);
        tripService.rateTrip(id, req.getRating(), passengerId);
        return ResponseEntity.ok(tripService.getTrip(id));
    }
}
