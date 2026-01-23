package com.filazero.appointmentservice.dto;

public record PatientScoreResponseDTO(
    Long patientId,
    String patientName,
    int currentScore,
    String criticalityLevel,
    Integer totalAppointments,
    Integer missedAppointments,
    Integer cancelledAppointments
) {}
