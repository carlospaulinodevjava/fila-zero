package com.filazero.appointmentservice.controller;

import com.filazero.appointmentservice.dto.PatientScoreResponseDTO;
import com.filazero.appointmentservice.persistence.entity.Patient;
import com.filazero.appointmentservice.persistence.repository.PatientRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patients")
public class PatientScoreController {

    private final PatientScoreService patientScoreService;
    private final PatientRepository patientRepository;

    public PatientScoreController(PatientScoreService patientScoreService, PatientRepository patientRepository) {
        this.patientScoreService = patientScoreService;
        this.patientRepository = patientRepository;
    }

    @GetMapping("/{id}/score")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE') or hasRole('ADMIN') or hasRole('PATIENT')")
    public ResponseEntity<PatientScoreResponseDTO> getPatientScore(@PathVariable Long id) {
        Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado com ID: " + id));

        int score = patientScoreService.getScore(id);

        PatientScoreResponseDTO response = new PatientScoreResponseDTO(
            patient.getId(),
            patient.getName(),
            score,
            "N/A",
            patient.getTotalAppointments() != null ? patient.getTotalAppointments() : 0,
            patient.getMissedAppointments() != null ? patient.getMissedAppointments() : 0,
            patient.getCancelledAppointments() != null ? patient.getCancelledAppointments() : 0
        );

        return ResponseEntity.ok(response);
    }
}
