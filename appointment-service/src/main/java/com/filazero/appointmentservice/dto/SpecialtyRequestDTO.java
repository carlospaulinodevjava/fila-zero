package com.filazero.appointmentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SpecialtyRequestDTO(
    @NotBlank(message = "Nome da especialidade é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    String name,
    
    String description,
    
    Integer averageWaitTime
) {}
