package com.taxi.notification.repo;

import com.taxi.common.enums.NotificationStatus;
import com.taxi.notification.entity.NotificationTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationTaskRepo extends JpaRepository<NotificationTask, Long> {
    List<NotificationTask> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    NotificationTask findTopByStatusOrderByCreatedAtAsc(NotificationStatus status);
}
