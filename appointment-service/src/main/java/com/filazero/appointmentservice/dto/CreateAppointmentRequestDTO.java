package com.filazero.appointmentservice.dto;

import com.filazero.appointmentservice.enums.AppointmentStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateAppointmentRequestDTO(
        @NotNull Long patientId,
        @NotNull Long doctorId,
        Long nurseId,
        @NotNull @Future LocalDateTime appointmentDate,
        @NotNull AppointmentStatus status,
        String notes
) {}
