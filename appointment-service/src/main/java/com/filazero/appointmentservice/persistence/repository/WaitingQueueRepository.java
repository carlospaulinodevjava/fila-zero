package com.filazero.appointmentservice.persistence.repository;

import com.filazero.appointmentservice.enums.WaitingQueueStatus;
import com.filazero.appointmentservice.persistence.entity.WaitingQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WaitingQueueRepository extends JpaRepository<WaitingQueue, Long> {

    @Query("SELECT w FROM WaitingQueue w WHERE w.specialty.id = :specialtyId AND w.status = :status ORDER BY w.priorityScore DESC, w.enteredAt ASC")
    List<WaitingQueue> findBySpecialtyAndStatusOrderByPriority(
        @Param("specialtyId") Long specialtyId,
        @Param("status") WaitingQueueStatus status
    );

    @Query("SELECT w FROM WaitingQueue w WHERE w.patient.id = :patientId AND w.status = :status")
    List<WaitingQueue> findByPatientIdAndStatus(
        @Param("patientId") Long patientId,
        @Param("status") WaitingQueueStatus status
    );

    @Query("SELECT w FROM WaitingQueue w WHERE w.status = 'AGUARDANDO' ORDER BY w.priorityScore DESC, w.enteredAt ASC")
    List<WaitingQueue> findAllWaitingOrderedByPriority();

    @Query("SELECT w FROM WaitingQueue w WHERE w.specialty.id = :specialtyId AND w.status = 'AGUARDANDO' ORDER BY w.priorityScore DESC, w.enteredAt ASC")
    List<WaitingQueue> findWaitingBySpecialtyOrderedByPriority(@Param("specialtyId") Long specialtyId);

    @Query("SELECT COUNT(w) FROM WaitingQueue w WHERE w.specialty.id = :specialtyId AND w.status = 'AGUARDANDO'")
    long countWaitingBySpecialty(@Param("specialtyId") Long specialtyId);

    @Query("SELECT w FROM WaitingQueue w WHERE w.patient.id = :patientId AND w.status IN ('AGUARDANDO', 'NOTIFICADO')")
    List<WaitingQueue> findActiveByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT w FROM WaitingQueue w WHERE w.status = 'AGUARDANDO' AND w.enteredAt < :time")
    List<WaitingQueue> findWaitingBeforeTime(@Param("time") LocalDateTime time);

    @Query("SELECT w FROM WaitingQueue w WHERE w.status = :status AND w.enteredAt BETWEEN :startDate AND :endDate")
    List<WaitingQueue> findByStatusAndDateRange(
        @Param("status") WaitingQueueStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
