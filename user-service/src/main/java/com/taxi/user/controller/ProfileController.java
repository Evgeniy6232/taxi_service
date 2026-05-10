package com.taxi.user.controller;

import com.taxi.common.enums.DriverStatus;
import com.taxi.user.dto.UpdateProfileRequest;
import com.taxi.user.entity.Driver;
import com.taxi.user.entity.Passenger;
import com.taxi.user.repo.DriverRepo;
import com.taxi.user.repo.PassengerRepo;
import com.taxi.user.repo.UserRepo;
import com.taxi.user.service.DriverCacheService;
import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final UserRepo userRepo;
    private final PassengerRepo passengerRepo;
    private final DriverRepo driverRepo;
    private final DriverCacheService driverCacheService;

    public ProfileController(UserRepo userRepo, PassengerRepo passengerRepo,
                             DriverRepo driverRepo, DriverCacheService driverCacheService) {
        this.userRepo = userRepo;
        this.passengerRepo = passengerRepo;
        this.driverRepo = driverRepo;
        this.driverCacheService = driverCacheService;
    }

    @GetMapping
    public ResponseEntity<?> getProfile(Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        Long refId = claims.get("refId", Long.class);
        String role = claims.get("role", String.class);

        if ("PASSENGER".equals(role)) {
            return passengerRepo.findById(refId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } else {
            return driverRepo.findById(refId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }
    }

    @PatchMapping
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest req,
                                           Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        Long refId = claims.get("refId", Long.class);
        String role = claims.get("role", String.class);

        if ("PASSENGER".equals(role)) {
            Passenger p = passengerRepo.findById(refId)
                    .orElseThrow(() -> new IllegalArgumentException("Пассажир не найден"));
            if (req.getName() != null) p.setName(req.getName());
            if (req.getPhone() != null) p.setPhone(req.getPhone());
            return ResponseEntity.ok(passengerRepo.save(p));
        } else {
            Driver d = driverRepo.findById(refId)
                    .orElseThrow(() -> new IllegalArgumentException("Водитель не найден"));
            if (req.getName() != null) d.setName(req.getName());
            if (req.getPhone() != null) d.setPhone(req.getPhone());
            if (req.getStatus() != null) {
                d.setStatus(DriverStatus.valueOf(req.getStatus()));
                Driver saved = driverRepo.save(d);
                switch (saved.getStatus()) {
                    case FREE -> driverCacheService.markFree(saved.getId());
                    case BUSY, OFFLINE -> driverCacheService.markBusy(saved.getId());
                }
                return ResponseEntity.ok(saved);
            }
            return ResponseEntity.ok(driverRepo.save(d));
        }
    }
}
