package com.filazero.appointmentservice.mapper;

import com.filazero.appointmentservice.dto.response.PatientResponseDTO;
import com.filazero.appointmentservice.persistence.entity.Patient;
import org.springframework.stereotype.Component;


@Component
public class PatientMapper {
    public PatientResponseDTO toResponseDTO(Patient patient) {
        if (patient == null) {
            return null;
        }
        
        return PatientResponseDTO.fromEntity(patient);
    }
}
