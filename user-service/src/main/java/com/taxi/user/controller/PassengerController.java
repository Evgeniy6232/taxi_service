package com.taxi.user.controller;

import com.taxi.user.entity.Passenger;
import com.taxi.user.repo.PassengerRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/passengers")
public class PassengerController {

    private final PassengerRepo passengerRepo;

    public PassengerController(PassengerRepo passengerRepo) {
        this.passengerRepo = passengerRepo;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Passenger> getPassenger(@PathVariable Long id) {
        return passengerRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
