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
           "AND a.appointmentDate BETWEEN :startDate AND :endDate " +
           "AND a.sentAt IS NULL")
    List<Appointment> findAppointmentsNeedingConfirmation(
        @Param("status") AppointmentStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

}
