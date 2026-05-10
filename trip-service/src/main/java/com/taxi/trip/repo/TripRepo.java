package com.taxi.trip.repo;

import com.taxi.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TripRepo extends JpaRepository<Trip, Long> {
    List<Trip> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);
    List<Trip> findByDriverIdOrderByCreatedAtDesc(Long driverId);
    List<Trip> findByDriverIdAndCreatedAtBetween(Long driverId, LocalDateTime start, LocalDateTime end);
    List<Trip> findByPassengerIdAndCreatedAtBetween(Long passengerId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(t) FROM Trip t WHERE CAST(t.createdAt AS date) = CURRENT_DATE")
    Long countToday();

    @Query("SELECT AVG(t.price) FROM Trip t WHERE CAST(t.createdAt AS date) = CURRENT_DATE")
    BigDecimal averagePriceToday();
}
