package com.filazero.appointmentservice.service.helper;

import com.filazero.appointmentservice.dto.DoctorRequestDTO;
import com.filazero.appointmentservice.persistence.entity.Doctor;
import com.filazero.appointmentservice.persistence.repository.DoctorRepository;
import com.filazero.appointmentservice.service.validator.DoctorValidator;
import org.springframework.stereotype.Component;

@Component
public class DoctorUpdater {

    private final DoctorValidator validator;
    private final DoctorRepository doctorRepository;

    public DoctorUpdater(DoctorValidator validator, DoctorRepository doctorRepository) {
        this.validator = validator;
        this.doctorRepository = doctorRepository;
    }

    public void updateDoctorFields(Doctor doctor, DoctorRequestDTO request, Long doctorId) {
        updateNameIfPresent(doctor, request.name());
        updateSpecialtyIfPresent(doctor, request.specialty());
        updateCRMIfPresent(doctor, request.crm(), doctorId);
    }

    private void updateNameIfPresent(Doctor doctor, String name) {
        if (isValidString(name)) {
            doctor.setName(name);
        }
    }

    private void updateSpecialtyIfPresent(Doctor doctor, String specialty) {
        if (specialty != null) {
            doctor.setSpecialty(specialty);
        }
    }

    private void updateCRMIfPresent(Doctor doctor, String crm, Long doctorId) {
        if (isValidString(crm)) {
            validator.validateCRMUniqueness(crm, doctorId, doctorRepository);
            doctor.setCrm(crm);
        }
    }

    private boolean isValidString(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
