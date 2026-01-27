package com.filazero.appointmentservice.dto;

import com.filazero.appointmentservice.enums.AppointmentStatus;
import com.filazero.appointmentservice.enums.CriticalityLevel;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateAppointmentRequestDTO(
        @NotNull Long patientId,
        @NotNull Long doctorId,
        Long nurseId,
        @NotNull @Future LocalDateTime appointmentDate,
        @NotNull AppointmentStatus status,
        CriticalityLevel criticidade,
        String notes
) {}
