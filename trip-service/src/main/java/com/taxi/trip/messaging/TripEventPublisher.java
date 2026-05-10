package com.taxi.trip.messaging;

import com.taxi.common.enums.TripStatus;
import com.taxi.common.messaging.TripEvent;
import com.taxi.trip.entity.Trip;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TripEventPublisher {

    public static final String EXCHANGE = "trip.events";

    private final RabbitTemplate rabbitTemplate;

    public TripEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishTripCreated(Trip trip) {
        TripEvent event = TripEvent.builder()
                .eventType("trip.created")
                .tripId(trip.getId())
                .passengerId(trip.getPassengerId())
                .driverId(trip.getDriverId())
                .recipientId(trip.getDriverId())
                .recipientType("DRIVER")
                .message("Новая поездка: " + trip.getOrigin() + " → " + trip.getDestination())
                .timestamp(Instant.now())
                .build();
        rabbitTemplate.convertAndSend(EXCHANGE, "", event);
    }

    public void publishStatusChange(Trip trip, TripStatus newStatus) {
        String eventType = switch (newStatus) {
            case IN_PROGRESS -> "trip.started";
            case COMPLETED -> "trip.completed";
            case CANCELLED -> "trip.cancelled";
            default -> null;
        };

        if (eventType == null) return;

        String recipientType = newStatus == TripStatus.CANCELLED ? "DRIVER" : "PASSENGER";
        Long recipientId = newStatus == TripStatus.CANCELLED
                ? trip.getDriverId()
                : trip.getPassengerId();

        String message = switch (newStatus) {
            case IN_PROGRESS -> "Водитель начал поездку";
            case COMPLETED -> "Поездка завершена";
            case CANCELLED -> "Пассажир отменил поездку";
            default -> "";
        };

        TripEvent event = TripEvent.builder()
                .eventType(eventType)
                .tripId(trip.getId())
                .passengerId(trip.getPassengerId())
                .driverId(trip.getDriverId())
                .recipientId(recipientId)
                .recipientType(recipientType)
                .message(message)
                .timestamp(Instant.now())
                .build();
        rabbitTemplate.convertAndSend(EXCHANGE, "", event);
    }
}
