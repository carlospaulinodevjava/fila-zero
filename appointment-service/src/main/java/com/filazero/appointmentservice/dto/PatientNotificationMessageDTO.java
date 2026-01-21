package com.filazero.appointmentservice.dto;

public record PatientNotificationMessageDTO(
        String name,
        String document,
        String phone,
        String email
) {
}
