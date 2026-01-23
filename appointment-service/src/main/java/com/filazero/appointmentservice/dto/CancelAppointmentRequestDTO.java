package com.filazero.appointmentservice.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelAppointmentRequestDTO(
    @NotBlank String reason
) {}
