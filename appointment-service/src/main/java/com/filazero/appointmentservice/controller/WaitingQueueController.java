package com.filazero.appointmentservice.controller;

import com.filazero.appointmentservice.dto.AddToQueueRequestDTO;
import com.filazero.appointmentservice.dto.QueuePositionResponseDTO;
import com.filazero.appointmentservice.persistence.entity.WaitingQueue;
import com.filazero.appointmentservice.service.WaitingQueueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/waiting-queues")
public class WaitingQueueController {

    private final WaitingQueueService waitingQueueService;

    public WaitingQueueController(WaitingQueueService waitingQueueService) {
        this.waitingQueueService = waitingQueueService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE') or hasRole('ADMIN') or hasRole('PATIENT')")
    public ResponseEntity<QueuePositionResponseDTO> addToQueue(@Valid @RequestBody AddToQueueRequestDTO request) {
        WaitingQueue queue = waitingQueueService.addToQueue(
            request.patientId(),
            request.specialtyId(),
            request.criticidade(),
            request.notes()
        );

        int position = waitingQueueService.getPatientPosition(queue.getPatient().getId(), queue.getSpecialty().getId());
        long totalInQueue = waitingQueueService.countWaitingBySpecialty(queue.getSpecialty().getId());

        QueuePositionResponseDTO response = new QueuePositionResponseDTO(
            queue.getId(),
            queue.getPatient().getId(),
            queue.getPatient().getName(),
            queue.getSpecialty().getName(),
            position,
            (int) totalInQueue,
            queue.getPriorityScore(),
            queue.getStatus().name(),
            queue.getEstimatedWaitTime()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE') or hasRole('ADMIN') or hasRole('PATIENT')")
    public ResponseEntity<List<QueuePositionResponseDTO>> getPatientQueues(@PathVariable Long patientId) {
        List<WaitingQueue> queues = waitingQueueService.getPatientActiveQueue(patientId);

        List<QueuePositionResponseDTO> response = queues.stream()
            .map(queue -> {
                int position = waitingQueueService.getPatientPosition(patientId, queue.getSpecialty().getId());
                long totalInQueue = waitingQueueService.countWaitingBySpecialty(queue.getSpecialty().getId());

                return new QueuePositionResponseDTO(
                    queue.getId(),
                    queue.getPatient().getId(),
                    queue.getPatient().getName(),
                    queue.getSpecialty().getName(),
                    position,
                    (int) totalInQueue,
                    queue.getPriorityScore(),
                    queue.getStatus().name(),
                    queue.getEstimatedWaitTime()
                );
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/specialty/{specialtyId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE') or hasRole('ADMIN')")
    public ResponseEntity<List<QueuePositionResponseDTO>> getQueueBySpecialty(@PathVariable Long specialtyId) {
        List<WaitingQueue> queues = waitingQueueService.getQueueBySpecialty(specialtyId);
        long totalInQueue = waitingQueueService.countWaitingBySpecialty(specialtyId);

        List<QueuePositionResponseDTO> response = queues.stream()
            .map(queue -> new QueuePositionResponseDTO(
                queue.getId(),
                queue.getPatient().getId(),
                queue.getPatient().getName(),
                queue.getSpecialty().getName(),
                waitingQueueService.getPatientPosition(queue.getPatient().getId(), specialtyId),
                (int) totalInQueue,
                queue.getPriorityScore(),
                queue.getStatus().name(),
                queue.getEstimatedWaitTime()
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{queueId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE') or hasRole('ADMIN') or hasRole('PATIENT')")
    public ResponseEntity<Void> removeFromQueue(@PathVariable Long queueId) {
        waitingQueueService.removeFromQueue(queueId);
        return ResponseEntity.noContent().build();
    }
}
