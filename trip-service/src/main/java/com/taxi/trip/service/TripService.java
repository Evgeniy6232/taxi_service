package com.taxi.trip.service;

import com.taxi.common.dto.StatsResponse;
import com.taxi.common.dto.TripCreateRequest;
import com.taxi.common.dto.TripResponse;
import com.taxi.common.enums.TripStatus;
import com.taxi.trip.client.UserServiceClient;
import com.taxi.trip.entity.Trip;
import com.taxi.trip.messaging.TripEventPublisher;
import com.taxi.trip.repo.TripRepo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class TripService {

    private final TripRepo tripRepo;
    private final UserServiceClient userServiceClient;
    private final PricingService pricingService;
    private final TripEventPublisher eventPublisher;

    public TripService(TripRepo tripRepo,
                       UserServiceClient userServiceClient,
                       PricingService pricingService,
                       TripEventPublisher eventPublisher) {
        this.tripRepo = tripRepo;
        this.userServiceClient = userServiceClient;
        this.pricingService = pricingService;
        this.eventPublisher = eventPublisher;
    }

    public TripResponse createTrip(Long passengerId, TripCreateRequest req) {
        // 1. Проверить, что пассажир существует
        if (!userServiceClient.passengerExists(passengerId)) {
            throw new IllegalArgumentException("Пассажир не найден");
        }

        // 2. Найти свободного водителя
        var freeDrivers = userServiceClient.getFreeDrivers();
        if (freeDrivers == null || freeDrivers.isEmpty()) {
            throw new IllegalStateException("Нет свободных водителей");
        }
        Long driverId = freeDrivers.get(0);

        // 3. Отметить водителя как занятого
        userServiceClient.updateDriverStatus(driverId, "BUSY");

        // 4. Посчитать цену
        var price = pricingService.calculatePrice(
                req.getOriginLat(), req.getOriginLng(),
                req.getDestLat(), req.getDestLng()
        );

        // 5. Сохранить поездку
        Trip trip = Trip.builder()
                .passengerId(passengerId)
                .driverId(driverId)
                .origin(req.getOrigin())
                .originLat(req.getOriginLat())
                .originLng(req.getOriginLng())
                .destination(req.getDestination())
                .destLat(req.getDestLat())
                .destLng(req.getDestLng())
                .status(TripStatus.WAITING)
                .price(price)
                .build();
        trip = tripRepo.save(trip);

        // 6. Отправить событие в RabbitMQ
        eventPublisher.publishTripCreated(trip);

        return mapToResponse(trip);
    }

    public List<TripResponse> getTripHistory(String role, Long refId) {
        List<Trip> trips;
        if ("PASSENGER".equals(role)) {
            trips = tripRepo.findByPassengerIdOrderByCreatedAtDesc(refId);
        } else {
            trips = tripRepo.findByDriverIdOrderByCreatedAtDesc(refId);
        }
        return trips.stream().map(this::mapToResponse).toList();
    }

    public List<TripResponse> getByPassenger(Long passengerId) {
        Long authUserId = getCurrentUserId();
        if (!authUserId.equals(passengerId)) {
            throw new IllegalArgumentException("Вы можете смотреть только свои поездки");
        }
        return tripRepo.findByPassengerIdOrderByCreatedAtDesc(passengerId)
                .stream().map(this::mapToResponse).toList();
    }

    public StatsResponse getDailyStats() {
        LocalDate today = LocalDate.now();
        Long count = tripRepo.countToday();
        BigDecimal avg = tripRepo.averagePriceToday();
        if (avg == null) avg = BigDecimal.ZERO;
        return new StatsResponse(
                today,
                count,
                avg.setScale(2, RoundingMode.HALF_UP)
        );
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long id) {
            return id;
        }
        throw new IllegalStateException("Пользователь не аутентифицирован");
    }

    public TripResponse getTrip(Long tripId) {
        Trip trip = tripRepo.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Поездка не найдена"));
        return mapToResponse(trip);
    }

    public TripResponse updateStatus(Long tripId, TripStatus newStatus,
                                     String role, Long refId) {
        Trip trip = tripRepo.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Поездка не найдена"));

        if ("PASSENGER".equals(role)) {
            if (newStatus != TripStatus.CANCELLED) {
                throw new IllegalArgumentException("Пассажир может только отменить поездку");
            }
            if (!trip.getPassengerId().equals(refId)) {
                throw new IllegalArgumentException("Это не ваша поездка");
            }
            if (trip.getStatus() != TripStatus.WAITING) {
                throw new IllegalArgumentException("Отменить можно только ожидающую поездку");
            }
        } else if ("DRIVER".equals(role)) {
            if (newStatus != TripStatus.IN_PROGRESS && newStatus != TripStatus.COMPLETED) {
                throw new IllegalArgumentException("Водитель может только начать или завершить поездку");
            }
            if (!trip.getDriverId().equals(refId)) {
                throw new IllegalArgumentException("Это не ваша поездка");
            }
            if (trip.getStatus() == TripStatus.WAITING && newStatus != TripStatus.IN_PROGRESS) {
                throw new IllegalArgumentException("Сначала нужно начать поездку");
            }
            if (trip.getStatus() == TripStatus.IN_PROGRESS && newStatus != TripStatus.COMPLETED) {
                throw new IllegalArgumentException("Поездка уже в процессе");
            }
            if (trip.getStatus() == TripStatus.COMPLETED || trip.getStatus() == TripStatus.CANCELLED) {
                throw new IllegalArgumentException("Нельзя изменить завершённую или отменённую поездку");
            }
        }

        trip.setStatus(newStatus);
        Trip saved = tripRepo.save(trip);

        // Освободить водителя при завершении или отмене
        if (newStatus == TripStatus.COMPLETED || newStatus == TripStatus.CANCELLED) {
            userServiceClient.updateDriverStatus(trip.getDriverId(), "FREE");
        }

        eventPublisher.publishStatusChange(saved, newStatus);

        return mapToResponse(saved);
    }

    public void rateTrip(Long tripId, int rating, Long passengerId) {
        Trip trip = tripRepo.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Поездка не найдена"));

        if (trip.getStatus() != TripStatus.COMPLETED) {
            throw new IllegalArgumentException("Оценить можно только завершённую поездку");
        }
        if (!trip.getPassengerId().equals(passengerId)) {
            throw new IllegalArgumentException("Это не ваша поездка");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Оценка должна быть от 1 до 5");
        }

        trip.setRating(rating);
        tripRepo.save(trip);
    }

    public StatsResponse getStats(LocalDate date, String role, Long refId) {
        var start = date.atStartOfDay();
        var end = date.plusDays(1).atStartOfDay();
        List<Trip> trips;
        if ("DRIVER".equals(role)) {
            trips = tripRepo.findByDriverIdAndCreatedAtBetween(refId, start, end);
        } else {
            trips = tripRepo.findByPassengerIdAndCreatedAtBetween(refId, start, end);
        }

        long count = trips.size();
        double avg = trips.stream()
                .map(Trip::getPrice)
                .filter(p -> p != null)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);

        return new StatsResponse(
                date,
                count,
                BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP)
        );
    }

    private TripResponse mapToResponse(Trip trip) {
        return TripResponse.builder()
                .id(trip.getId())
                .passengerId(trip.getPassengerId())
                .driverId(trip.getDriverId())
                .origin(trip.getOrigin())
                .originLat(trip.getOriginLat())
                .originLng(trip.getOriginLng())
                .destination(trip.getDestination())
                .destLat(trip.getDestLat())
                .destLng(trip.getDestLng())
                .status(trip.getStatus())
                .price(trip.getPrice())
                .rating(trip.getRating())
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .build();
    }
}
