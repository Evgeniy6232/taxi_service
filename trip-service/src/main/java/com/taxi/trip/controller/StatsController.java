package com.taxi.trip.controller;

import com.taxi.common.dto.StatsResponse;
import com.taxi.trip.service.TripService;
import io.jsonwebtoken.Claims;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final TripService tripService;

    public StatsController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping("/daily")
    public ResponseEntity<StatsResponse> getDailyStats() {
        return ResponseEntity.ok(tripService.getDailyStats());
    }

    @GetMapping
    public ResponseEntity<StatsResponse> getStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        String role = claims.get("role", String.class);
        Long refId = claims.get("refId", Long.class);
        return ResponseEntity.ok(tripService.getStats(date, role, refId));
    }
}
