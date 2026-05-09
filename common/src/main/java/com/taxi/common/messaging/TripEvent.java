package com.taxi.common.messaging;

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

    private static final long serialVersionUID = 1L;

    private String eventType;
    private Long tripId;
    private Long passengerId;
    private Long driverId;
    private Long recipientId;
    private String recipientType;
    private String message;
    private Instant timestamp;
}

//изначально закинул его в сам микросервис трип, но дойдя до нотификейшена понял, что лучше перенести сюда