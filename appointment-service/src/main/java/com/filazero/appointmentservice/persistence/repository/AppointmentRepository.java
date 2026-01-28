package com.filazero.appointmentservice.persistence.repository;

import com.filazero.appointmentservice.enums.AppointmentStatus;
import com.filazero.appointmentservice.persistence.entity.Appointment;
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

    @Query("SELECT a FROM Appointment a WHERE a.status = :status " +
            "AND a.appointmentDate >= :startDate " +
            "AND a.sentAt IS NULL")
    List<Appointment> findAppointmentsNeedingConfirmation(
            @Param("status") AppointmentStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT a " +
            "    FROM Appointment a " +
            "    WHERE a.status = 'VAGA_ABERTA' " +
            "      AND a.appointmentDate > :agora " +
            "    ORDER BY a.appointmentDate ASC")
    List<Appointment> buscarVagasAbertas(@Param("agora") LocalDateTime agora);

    @Query("""
    SELECT a
    FROM Appointment a
    WHERE a.doctor.id = :doctorId
      AND a.appointmentDate >= :dataMinima
      AND a.status IN ('AGENDADO', 'CONFIRMADO')
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
            @Param("agora") LocalDateTime agora
    );
}
