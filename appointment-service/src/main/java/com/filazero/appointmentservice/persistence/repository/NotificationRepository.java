package com.filazero.appointmentservice.persistence.repository;

import com.filazero.appointmentservice.persistence.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByTrackingToken(String trackingToken);
}
