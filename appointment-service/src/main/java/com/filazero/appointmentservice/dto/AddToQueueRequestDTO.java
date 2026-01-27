package com.filazero.appointmentservice.dto;

import com.filazero.appointmentservice.enums.CriticalityLevel;
import jakarta.validation.constraints.NotNull;

public record AddToQueueRequestDTO(
    @NotNull Long patientId,
    @NotNull Long specialtyId,
    @NotNull CriticalityLevel criticidade,
    Long preferredDoctorId,
    String notes
) {}
