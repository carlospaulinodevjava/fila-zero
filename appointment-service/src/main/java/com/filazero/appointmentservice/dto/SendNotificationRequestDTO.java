package com.filazero.appointmentservice.dto;

public record SendNotificationRequestDTO(
        String patientId,
        String appointmentId,
        String message,
        String channel
) {
}
