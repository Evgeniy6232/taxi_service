package com.taxi.notification.service;

import com.taxi.common.enums.NotificationStatus;
import com.taxi.notification.entity.NotificationTask;
import com.taxi.notification.repo.NotificationTaskRepo;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class NotificationWorkerPool {

    private static final Logger log = LoggerFactory.getLogger(NotificationWorkerPool.class);
    private static final int WORKER_COUNT = 4;
    private static final int MAX_ATTEMPTS = 3;

    private final NotificationTaskRepo taskRepo;
    private final ExecutorService executor = Executors.newFixedThreadPool(WORKER_COUNT);
    private volatile boolean running = true;

    public NotificationWorkerPool(NotificationTaskRepo taskRepo) {
        this.taskRepo = taskRepo;
    }

    @PostConstruct
    public void start() {
        log.info("Starting {} notification workers", WORKER_COUNT);
        for (int i = 0; i < WORKER_COUNT; i++) {
            executor.submit(this::workerLoop);
        }
    }

    private void workerLoop() {
        String threadName = Thread.currentThread().getName();
        log.info("Worker {} started", threadName);

        while (running) {
            try {
                NotificationTask task = claimNextPending();
                if (task != null) {
                    processTask(task);
                } else {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.info("Worker {} stopped", threadName);
    }

    @Transactional
    private NotificationTask claimNextPending() {
        try {
            NotificationTask task = taskRepo.findTopByStatusOrderByCreatedAtAsc(NotificationStatus.PENDING);
            if (task != null) {
                task.setStatus(NotificationStatus.PROCESSING);
                taskRepo.save(task);
            }
            return task;
        } catch (ObjectOptimisticLockingFailureException e) {
            log.debug("Optimistic lock conflict, retrying next task");
            return null;
        }
    }

    private void processTask(NotificationTask task) {
        try {
            log.info("Processing notification #{}: {}", task.getId(), task.getMessage());
            Thread.sleep(500);
            task.setStatus(NotificationStatus.SENT);
        } catch (Exception e) {
            log.error("Failed to send notification #{}", task.getId(), e);
            task.setAttempts(task.getAttempts() + 1);
            task.setStatus(task.getAttempts() < MAX_ATTEMPTS
                    ? NotificationStatus.PENDING
                    : NotificationStatus.FAILED);
        }
        taskRepo.save(task);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down notification workers...");
        running = false;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                log.warn("Workers did not finish in time, forcing shutdown");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("All workers stopped");
    }
}
