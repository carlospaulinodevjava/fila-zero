package com.filazero.appointmentservice.mapper;

import com.filazero.appointmentservice.dto.response.NurseResponseDTO;
import com.filazero.appointmentservice.persistence.entity.Nurse;
import org.springframework.stereotype.Component;

@Component
public class NurseMapper {
    public NurseResponseDTO toResponseDTO(Nurse nurse) {
        if (nurse == null) {
            return null;
        }

        return NurseResponseDTO.fromEntity(nurse);
    }
}
