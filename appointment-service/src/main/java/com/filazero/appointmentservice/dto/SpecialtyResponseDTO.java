package com.filazero.appointmentservice.dto;

public record SpecialtyResponseDTO(
    Long id,
    String name,
    String description,
    Integer averageWaitTime
) {}
