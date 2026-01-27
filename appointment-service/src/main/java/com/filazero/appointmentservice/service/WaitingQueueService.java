package com.filazero.appointmentservice.service;

import com.filazero.appointmentservice.enums.CriticalityLevel;
import com.filazero.appointmentservice.enums.WaitingQueueStatus;
import com.filazero.appointmentservice.persistence.entity.Patient;
import com.filazero.appointmentservice.persistence.entity.Specialty;
import com.filazero.appointmentservice.persistence.entity.WaitingQueue;
import com.filazero.appointmentservice.persistence.repository.PatientRepository;
import com.filazero.appointmentservice.persistence.repository.SpecialtyRepository;
import com.filazero.appointmentservice.persistence.repository.WaitingQueueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WaitingQueueService {

    private final WaitingQueueRepository waitingQueueRepository;
    private final PatientRepository patientRepository;
    private final SpecialtyRepository specialtyRepository;

    private static final int CRITICALITY_WEIGHT_MULTIPLIER = 1000;
    private static final int DAYS_WAITING_MULTIPLIER = 10;

    public WaitingQueueService(WaitingQueueRepository waitingQueueRepository,
                               PatientRepository patientRepository,
                               SpecialtyRepository specialtyRepository) {
        this.waitingQueueRepository = waitingQueueRepository;
        this.patientRepository = patientRepository;
        this.specialtyRepository = specialtyRepository;
    }

    public WaitingQueue addToQueue(Long patientId, Long specialtyId, CriticalityLevel criticidade, String notes) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado com ID: " + patientId));
        
        Specialty specialty = specialtyRepository.findById(specialtyId)
            .orElseThrow(() -> new IllegalArgumentException("Especialidade não encontrada com ID: " + specialtyId));

        WaitingQueue queue = new WaitingQueue();
        queue.setPatient(patient);
        queue.setSpecialty(specialty);
        queue.setCriticidade(criticidade != null ? criticidade : CriticalityLevel.NORMAL);
        queue.setEnteredAt(LocalDateTime.now());
        queue.setPriorityScore(calculatePriorityScore(criticidade, LocalDateTime.now()));
        queue.setStatus(WaitingQueueStatus.AGUARDANDO);
        queue.setEstimatedWaitTime(specialty.getAverageWaitTime());
        queue.setNotes(notes);

        return waitingQueueRepository.save(queue);
    }

    public int calculatePriorityScore(CriticalityLevel criticidade, LocalDateTime enteredAt) {
        CriticalityLevel criticality = criticidade != null ? criticidade : CriticalityLevel.NORMAL;
        
        int criticalityScore = criticality.getPeso() * CRITICALITY_WEIGHT_MULTIPLIER;
        
        long daysWaiting = ChronoUnit.DAYS.between(enteredAt, LocalDateTime.now());
        int waitingScore = (int) (daysWaiting * DAYS_WAITING_MULTIPLIER);
        
        return criticalityScore + waitingScore;
    }

    public Optional<WaitingQueue> findNextInQueue(Long specialtyId) {
        List<WaitingQueue> waitingList = waitingQueueRepository.findWaitingBySpecialtyOrderedByPriority(specialtyId);
        return waitingList.isEmpty() ? Optional.empty() : Optional.of(waitingList.get(0));
    }

    public int getPatientPosition(Long patientId, Long specialtyId) {
        List<WaitingQueue> queue = waitingQueueRepository.findWaitingBySpecialtyOrderedByPriority(specialtyId);
        
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getPatient().getId().equals(patientId)) {
                return i + 1;
            }
        }
        
        return -1;
    }

    public void markAsNotified(Long queueId) {
        WaitingQueue queue = waitingQueueRepository.findById(queueId)
            .orElseThrow(() -> new IllegalArgumentException("Fila não encontrada com ID: " + queueId));
        
        queue.setStatus(WaitingQueueStatus.NOTIFICADO);
        queue.setNotifiedAt(LocalDateTime.now());
        waitingQueueRepository.save(queue);
    }

    public void markAsScheduled(Long queueId) {
        WaitingQueue queue = waitingQueueRepository.findById(queueId)
            .orElseThrow(() -> new IllegalArgumentException("Fila não encontrada com ID: " + queueId));
        
        queue.setStatus(WaitingQueueStatus.AGENDADO);
        waitingQueueRepository.save(queue);
    }

    public void removeFromQueue(Long queueId) {
        WaitingQueue queue = waitingQueueRepository.findById(queueId)
            .orElseThrow(() -> new IllegalArgumentException("Fila não encontrada com ID: " + queueId));
        
        queue.setStatus(WaitingQueueStatus.CANCELADO);
        waitingQueueRepository.save(queue);
    }

    public List<WaitingQueue> getQueueBySpecialty(Long specialtyId) {
        return waitingQueueRepository.findWaitingBySpecialtyOrderedByPriority(specialtyId);
    }

    public List<WaitingQueue> getPatientActiveQueue(Long patientId) {
        return waitingQueueRepository.findActiveByPatientId(patientId);
    }

    public long countWaitingBySpecialty(Long specialtyId) {
        return waitingQueueRepository.countWaitingBySpecialty(specialtyId);
    }
}
