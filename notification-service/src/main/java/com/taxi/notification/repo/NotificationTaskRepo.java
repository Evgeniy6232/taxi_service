package com.taxi.notification.repo;

import com.taxi.common.enums.NotificationStatus;
import com.taxi.notification.entity.NotificationTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationTaskRepo extends JpaRepository<NotificationTask, Long> {
    List<NotificationTask> findByRecipientIdAndRecipientTypeOrderByCreatedAtDesc(Long recipientId, String recipientType);
    List<NotificationTask> findByTripId(Long tripId);

    NotificationTask findTopByStatusOrderByCreatedAtAsc(NotificationStatus status);
}
