package com.filazero.appointmentservice.dto.response;

import com.filazero.appointmentservice.persistence.entity.Doctor;

import java.util.Comparator;
import java.util.List;

public record DoctorResponseDTO(
        Long id,
        Long userId,
        String name,
        List<String> specialties,
        String crm
) {

    public static DoctorResponseDTO fromEntity(Doctor doctor) {
        return new DoctorResponseDTO(
                doctor.getId(),
                doctor.getUser().getId(),
                doctor.getName(),
                doctor.getSpecialties().stream()
                        .map(s -> s.getName())
                        .sorted(Comparator.naturalOrder())
                        .toList(),
                doctor.getCrm()
        );
    }
}