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

    @Query("""
                SELECT n
                FROM Notification n
                WHERE n.isExpired = false
                  AND (
                        (n.status = :statusEnviado AND n.expiresAt IS NOT NULL AND n.expiresAt < :now)
                     OR (n.status = :statusCancelado)
                  )
            """)
    List<Notification> findExpiredOrCancelledNotifications(
            @Param("statusEnviado") NotificationStatus statusEnviado,
            @Param("statusCancelado") NotificationStatus statusCancelado,
            @Param("now") LocalDateTime now
    );
}
