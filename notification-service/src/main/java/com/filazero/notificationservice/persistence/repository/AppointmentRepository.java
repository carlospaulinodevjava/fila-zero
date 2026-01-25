package com.filazero.notificationservice.persistence.repository;

import com.filazero.notificationservice.enums.AppointmentStatus;
import com.filazero.notificationservice.enums.NotificationType;
import com.filazero.notificationservice.persistence.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findById(Long id);

    Page<Appointment> findAll(Pageable pageable);

    @Query("""
        SELECT a
        FROM Appointment a
        WHERE a.status = :status
          AND a.appointmentDate >= :inicio
          AND a.appointmentDate <= :fim
          AND NOT EXISTS (
              SELECT 1
              FROM Notification n
              WHERE n.appointment = a
                AND n.type = :tipoNotificacao
          )
        ORDER BY a.appointmentDate ASC
    """)
    List<Appointment> buscarConsultasProximasNaoNotificadas(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("tipoNotificacao") NotificationType tipoNotificacao,
            @Param("status") AppointmentStatus status
    );}
