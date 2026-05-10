package com.taxi.notification.controller;

import com.taxi.notification.entity.NotificationTask;
import com.taxi.notification.repo.NotificationTaskRepo;
import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationTaskRepo taskRepo;

    public NotificationController(NotificationTaskRepo taskRepo) {
        this.taskRepo = taskRepo;
    }

    @GetMapping(params = "trip_id")
    public ResponseEntity<List<NotificationTask>> getByTrip(@RequestParam("trip_id") Long tripId) {
        return ResponseEntity.ok(taskRepo.findByTripId(tripId));
    }

    @GetMapping
    public ResponseEntity<List<NotificationTask>> getNotifications(Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        Long refId = claims.get("refId", Long.class);
        String role = claims.get("role", String.class);
        return ResponseEntity.ok(taskRepo.findByRecipientIdAndRecipientTypeOrderByCreatedAtDesc(refId, role));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id,
                                                          Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        Long refId = claims.get("refId", Long.class);

        NotificationTask task = taskRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Уведомление не найдено"));

        if (!task.getRecipientId().equals(refId)) {
            throw new IllegalArgumentException("Это не ваше уведомление");
        }

        task.setRead(true);
        taskRepo.save(task);

        return ResponseEntity.ok(Map.of("message", "Отмечено как прочитанное"));
    }
}
