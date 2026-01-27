package com.filazero.appointmentservice.dto;

import com.filazero.appointmentservice.enums.AppointmentStatus;
import com.filazero.appointmentservice.enums.CriticalityLevel;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class UpdateAppointmentRequestDTO {
    @NotNull
    private Long doctorId;

    @NotNull
    private Long patientId;

    private Long nurseId;

    @NotNull
    @Future
    private LocalDateTime appointmentDate;

    @NotNull
    private AppointmentStatus status;

    private CriticalityLevel criticidade;

    private String notes;

    // Getters e Setters
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getNurseId() { return nurseId; }
    public void setNurseId(Long nurseId) { this.nurseId = nurseId; }

    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public CriticalityLevel getCriticidade() { return criticidade; }
    public void setCriticidade(CriticalityLevel criticidade) { this.criticidade = criticidade; }
}
