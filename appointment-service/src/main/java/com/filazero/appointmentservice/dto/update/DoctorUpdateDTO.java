package com.filazero.appointmentservice.dto.update;

import jakarta.validation.constraints.Size;

import java.util.List;

public record DoctorUpdateDTO (

    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    String name,

    List<@Size(max = 100, message = "Especialidade deve ter no máximo 100 caracteres") String> specialties,

    @Size(max = 30, message = "Especialidade deve ter no máximo 30 caracteres")
    String crm
) {}
