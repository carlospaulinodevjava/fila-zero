package com.filazero.appointmentservice.dto;

public record QueuePositionResponseDTO(
    Long queueId,
    Long patientId,
    String patientName,
    String specialty,
    int position,
    int totalInQueue,
    int priorityScore,
    String status,
    Integer estimatedWaitDays
) {}
