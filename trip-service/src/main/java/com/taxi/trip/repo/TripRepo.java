package com.taxi.trip.repo;

import com.taxi.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TripRepo extends JpaRepository<Trip, Long> {
    List<Trip> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);
    List<Trip> findByDriverIdOrderByCreatedAtDesc(Long driverId);
    List<Trip> findByDriverIdAndCreatedAtBetween(Long driverId, LocalDateTime start, LocalDateTime end);
}
