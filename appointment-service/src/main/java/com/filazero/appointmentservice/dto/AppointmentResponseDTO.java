package com.filazero.appointmentservice.dto;

import com.filazero.appointmentservice.enums.AppointmentStatus;
import com.filazero.appointmentservice.enums.CriticalityLevel;

import java.time.LocalDateTime;

public class AppointmentResponseDTO {
    private Long id;
    private Long doctorId;
    private Long patientId;
    private LocalDateTime appointmentDate;
    private AppointmentStatus status;
    private CriticalityLevel criticidade;
    private String notes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public CriticalityLevel getCriticidade() { return criticidade; }
    public void setCriticidade(CriticalityLevel criticidade) { this.criticidade = criticidade; }
}
