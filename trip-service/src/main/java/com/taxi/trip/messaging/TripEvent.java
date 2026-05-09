package com.taxi.trip.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripEvent implements Serializable {
    // Serializable - это интерфейс, который дает нам информацию о том,
    // что мы можем преобразовать наш объект в какой-то текст,
    // а затем десериализовать его, чтобы снова превратить этот текст в объект.

    private static final long serialVersionUID = 1L;

    private String eventType;
    private Long tripId;
    private Long passengerId;
    private Long driverId;
    private Long recipientId; // кому отправлять уведомление
    private String recipientType; // клиент или водила
    private String message; // текст уведа
    private Instant timestamp;
}
