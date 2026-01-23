package com.filazero.appointmentservice.service;

import com.filazero.appointmentservice.enums.AppointmentStatus;
import com.filazero.appointmentservice.persistence.entity.Patient;
import com.filazero.appointmentservice.persistence.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PatientScoreService {

    private final PatientRepository patientRepository;

    private static final int SCORE_CONFIRMED = 10;
    private static final int SCORE_COMPLETED = 20;
    private static final int SCORE_CANCELLED = -15;
    private static final int SCORE_NO_SHOW = -25;
    private static final int SCORE_ON_TIME = 5;
    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 200;

    public PatientScoreService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public void updateScore(Long patientId, AppointmentStatus status) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado com ID: " + patientId));

        int currentScore = patient.getEngagementScore() != null ? patient.getEngagementScore() : 100;
        int scoreChange = 0;

        switch (status) {
            case CONFIRMADO -> {
                scoreChange = SCORE_CONFIRMED;
                incrementTotalAppointments(patient);
            }
            case REALIZADO -> {
                scoreChange = SCORE_COMPLETED;
            }
            case CANCELADO_PELO_PACIENTE -> {
                scoreChange = SCORE_CANCELLED;
                incrementCancelledAppointments(patient);
            }
            case CANCELADO_POR_INCONFIRMACAO -> {
                scoreChange = SCORE_NO_SHOW;
                incrementMissedAppointments(patient);
            }
        }

        int newScore = Math.max(MIN_SCORE, Math.min(MAX_SCORE, currentScore + scoreChange));
        patient.setEngagementScore(newScore);
        patientRepository.save(patient);
    }

    public void rewardOnTime(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado com ID: " + patientId));

        int currentScore = patient.getEngagementScore() != null ? patient.getEngagementScore() : 100;
        int newScore = Math.min(MAX_SCORE, currentScore + SCORE_ON_TIME);
        patient.setEngagementScore(newScore);
        patientRepository.save(patient);
    }

    public int getScore(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado com ID: " + patientId));
        
        return patient.getEngagementScore() != null ? patient.getEngagementScore() : 100;
    }

    private void incrementTotalAppointments(Patient patient) {
        int total = patient.getTotalAppointments() != null ? patient.getTotalAppointments() : 0;
        patient.setTotalAppointments(total + 1);
    }

    private void incrementMissedAppointments(Patient patient) {
        int missed = patient.getMissedAppointments() != null ? patient.getMissedAppointments() : 0;
        patient.setMissedAppointments(missed + 1);
    }

    private void incrementCancelledAppointments(Patient patient) {
        int cancelled = patient.getCancelledAppointments() != null ? patient.getCancelledAppointments() : 0;
        patient.setCancelledAppointments(cancelled + 1);
    }
}
