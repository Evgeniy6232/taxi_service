package com.taxi.trip.controller;

import com.taxi.common.dto.TripCreateRequest;
import com.taxi.common.dto.TripResponse;
import com.taxi.trip.dto.RateTripRequest;
import com.taxi.trip.dto.UpdateTripStatusRequest;
import com.taxi.trip.service.TripService;
import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/trips")
// тут тоже важно понимать что при создании заказа нужен айди из токена чтоб избежать багов
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    public ResponseEntity<TripResponse> createTrip(@RequestBody TripCreateRequest req,
                                                   Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        Long passengerId = claims.get("refId", Long.class);
        return ResponseEntity.ok(tripService.createTrip(passengerId, req));
    }

    @GetMapping
    public ResponseEntity<List<TripResponse>> getHistory(Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        Long userId = Long.parseLong(claims.getSubject());
        String role = claims.get("role", String.class);
        Long refId = claims.get("refId", Long.class);
        return ResponseEntity.ok(tripService.getTripHistory(userId, role, refId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> getTrip(@PathVariable Long id) {
        return ResponseEntity.ok(tripService.getTrip(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TripResponse> updateStatus(@PathVariable Long id,
                                                     @RequestBody UpdateTripStatusRequest req,
                                                     Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        String role = claims.get("role", String.class);
        Long refId = claims.get("refId", Long.class);
        return ResponseEntity.ok(tripService.updateStatus(id, req.getStatus(), role, refId));
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<Map<String, String>> rateTrip(@PathVariable Long id,
                                                        @RequestBody RateTripRequest req,
                                                        Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        Long passengerId = claims.get("refId", Long.class);
        tripService.rateTrip(id, req.getRating(), passengerId);
        return ResponseEntity.ok(Map.of("message", "Оценка поставлена"));
    }
}
