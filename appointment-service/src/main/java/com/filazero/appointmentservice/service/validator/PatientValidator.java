package com.filazero.appointmentservice.service.validator;

import com.filazero.appointmentservice.dto.PatientRequestDTO;
import com.filazero.appointmentservice.exception.DocumentAlreadyExistsException;
import com.filazero.appointmentservice.persistence.entity.Patient;
import com.filazero.appointmentservice.persistence.repository.PatientRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PatientValidator {

    public void validateUpdateRequest(PatientRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request não pode ser nulo");
        }
    }

    public void validateDocumentUniqueness(String document, Long currentPatientId, PatientRepository repository) {
        if (document != null && !document.trim().isEmpty()) {
            Optional<Patient> existingPatient = repository.findByDocument(document);
            if (existingPatient.isPresent() && !existingPatient.get().getId().equals(currentPatientId)) {
                throw new DocumentAlreadyExistsException("Documento já existe para outro paciente");
            }
        }
    }
}
