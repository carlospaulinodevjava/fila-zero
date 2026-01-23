package com.filazero.appointmentservice.dto;

import jakarta.validation.constraints.NotNull;

public record AddToQueueRequestDTO(
    @NotNull Long patientId,
    @NotNull Long specialtyId,
    Long preferredDoctorId,
    String notes
) {}
