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
    );

    @Query("""
        SELECT a
        FROM Appointment a
        WHERE a.status = :status
          AND a.confirmationDeadline IS NOT NULL
          AND a.confirmationDeadline < :agora
        ORDER BY a.confirmationDeadline ASC
    """)
    List<Appointment> buscarAppointmentsNaoConfirmados(
            @Param("status") AppointmentStatus status,
            @Param("agora") LocalDateTime agora
    );

    @Query("""
        SELECT a
        FROM Appointment a
        WHERE a.status = 'VAGA_ABERTA'
          AND a.appointmentDate > :agora
        ORDER BY a.appointmentDate ASC
    """)
    List<Appointment> buscarVagasAbertas(@Param("agora") LocalDateTime agora);

    @Query("""
        SELECT a
        FROM Appointment a
        WHERE a.doctor.id = :doctorId
          AND a.appointmentDate >= :dataMinima
          AND a.status IN ('AGENDADO', 'CONFIRMADO')
          AND (a.offerExpiresAt IS NULL OR a.offerExpiresAt < :agora)
          AND a.status <> 'REMARCACAO_OFERECIDA'
          AND NOT EXISTS (
              SELECT 1
              FROM Notification n
              WHERE n.appointment = a
                AND n.type = 'REALOCACAO'
                AND n.status = 'ENVIADO'
                AND (n.isExpired = false OR n.isExpired IS NULL)
                AND (n.expiresAt IS NULL OR n.expiresAt > :agora)
          )
        ORDER BY a.appointmentDate ASC
    """)
    List<Appointment> buscarCandidatosElegiveisParaAntecipacao(
            @Param("doctorId") Long doctorId,
            @Param("dataMinima") LocalDateTime dataMinima,
            @Param("agora") LocalDateTime agora,
            Pageable pageable
    );
}


