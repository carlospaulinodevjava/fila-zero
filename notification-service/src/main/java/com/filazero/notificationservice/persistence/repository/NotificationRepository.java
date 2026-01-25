package com.filazero.notificationservice.persistence.repository;

import com.filazero.notificationservice.persistence.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<com.filazero.notificationservice.persistence.entity.Notification> findByTrackingToken(String trackingToken);
}
