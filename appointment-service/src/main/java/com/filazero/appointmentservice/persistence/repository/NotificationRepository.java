package com.filazero.appointmentservice.persistence.repository;

import com.filazero.appointmentservice.enums.NotificationStatus;
import com.filazero.appointmentservice.enums.NotificationType;
import com.filazero.appointmentservice.persistence.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByTrackingToken(String trackingToken);

    boolean existsByAppointmentIdAndTypeIn(Long appointmentId, List<NotificationType> types);

    @Query("SELECT n FROM Notification n WHERE n.status = :status " +
           "AND n.expiresAt < :now " +
           "AND n.isExpired = false")
    List<Notification> findExpiredNotifications(
        @Param("status") NotificationStatus status,
        @Param("now") LocalDateTime now
    );
}
