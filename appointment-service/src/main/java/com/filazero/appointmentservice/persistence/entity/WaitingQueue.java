package com.filazero.appointmentservice.persistence.entity;

import com.filazero.appointmentservice.enums.CriticalityLevel;
import com.filazero.appointmentservice.enums.WaitingQueueStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "waiting_queues", indexes = {
    @Index(name = "idx_specialty_status", columnList = "specialty_id, status"),
    @Index(name = "idx_priority_score", columnList = "priority_score DESC")
})
public class WaitingQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    @ManyToOne
    @JoinColumn(name = "preferred_doctor_id")
    private Doctor preferredDoctor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WaitingQueueStatus status;

    @Column(name = "priority_score", nullable = false)
    private Integer priorityScore;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CriticalityLevel criticidade;

    @Column(name = "entered_at", nullable = false)
    private LocalDateTime enteredAt;

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    @Column(name = "estimated_wait_time")
    private Integer estimatedWaitTime;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Specialty getSpecialty() {
        return specialty;
    }

    public void setSpecialty(Specialty specialty) {
        this.specialty = specialty;
    }

    public Doctor getPreferredDoctor() {
        return preferredDoctor;
    }

    public void setPreferredDoctor(Doctor preferredDoctor) {
        this.preferredDoctor = preferredDoctor;
    }

    public WaitingQueueStatus getStatus() {
        return status;
    }

    public void setStatus(WaitingQueueStatus status) {
        this.status = status;
    }

    public Integer getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(Integer priorityScore) {
        this.priorityScore = priorityScore;
    }

    public LocalDateTime getEnteredAt() {
        return enteredAt;
    }

    public void setEnteredAt(LocalDateTime enteredAt) {
        this.enteredAt = enteredAt;
    }

    public LocalDateTime getNotifiedAt() {
        return notifiedAt;
    }

    public void setNotifiedAt(LocalDateTime notifiedAt) {
        this.notifiedAt = notifiedAt;
    }

    public Integer getEstimatedWaitTime() {
        return estimatedWaitTime;
    }

    public void setEstimatedWaitTime(Integer estimatedWaitTime) {
        this.estimatedWaitTime = estimatedWaitTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public CriticalityLevel getCriticidade() {
        return criticidade;
    }

    public void setCriticidade(CriticalityLevel criticidade) {
        this.criticidade = criticidade;
    }
}
