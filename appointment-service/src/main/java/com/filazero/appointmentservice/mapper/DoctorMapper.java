package com.filazero.appointmentservice.mapper;

import com.filazero.appointmentservice.dto.response.DoctorResponseDTO;
import com.filazero.appointmentservice.persistence.entity.Doctor;
import org.springframework.stereotype.Component;


@Component
public class DoctorMapper {
    public DoctorResponseDTO toResponseDTO(Doctor doctor) {
        if (doctor == null) {
            return null;
        }
        
        return DoctorResponseDTO.fromEntity(doctor);
    }
}
