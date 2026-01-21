package com.filazero.appointmentservice.dto.response;

import com.filazero.appointmentservice.persistence.entity.Nurse;

public record NurseResponseDTO(
        Long id,
        Long userId,
        String name,
        String coren
) {

    public static NurseResponseDTO fromEntity(Nurse nurse) {
        return new NurseResponseDTO(
                nurse.getId(),
                nurse.getUser().getId(),
                nurse.getName(),
                nurse.getCoren()
        );
    }
}
