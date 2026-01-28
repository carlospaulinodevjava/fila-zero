package com.filazero.appointmentservice.dto;

import java.util.List;

public record DoctorRequestDTO(
        Long id,
        Long userId,
        String name,
        List<String> specialties,
        String crm
) {
}