package com.taxi.notification.messaging;

import com.taxi.common.messaging.TripEvent;
import com.taxi.notification.entity.NotificationTask;
import com.taxi.notification.repo.NotificationTaskRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TripEventListener {

    private static final Logger log = LoggerFactory.getLogger(TripEventListener.class);

    private final NotificationTaskRepo taskRepo;

    public TripEventListener(NotificationTaskRepo taskRepo) {
        this.taskRepo = taskRepo;
    }

    @RabbitListener(queues = "trip.events.queue")
    public void handleTripEvent(TripEvent event) {
        log.info("Received event: {} for trip #{}", event.getEventType(), event.getTripId());

        NotificationTask task = NotificationTask.builder()
                .tripId(event.getTripId())
                .recipientId(event.getRecipientId())
                .recipientType(event.getRecipientType())
                .type(event.getEventType())
                .message(event.getMessage())
                .build();

        taskRepo.save(task);
        log.info("Notification task #{} created for recipient {}", task.getId(), task.getRecipientId());
    }
}

/* получение смс
*  обработка (handle trip event)
*  либо выброс ошибки, либо подтверждение и удаление из очереди
*
*
*
* */