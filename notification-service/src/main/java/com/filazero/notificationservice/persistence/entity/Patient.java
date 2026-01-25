package com.filazero.notificationservice.persistence.entity;

import com.filazero.notificationservice.enums.CriticalityLevel;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(nullable = false, unique = true, length = 50)
    private String document;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CriticalityLevel criticidade;

    @Column(name = "engagement_score")
    private Integer engagementScore;

    @Column(name = "total_appointments")
    private Integer totalAppointments;

    @Column(name = "missed_appointments")
    private Integer missedAppointments;

    @Column(name = "cancelled_appointments")
    private Integer cancelledAppointments;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public CriticalityLevel getCriticidade() {
        return criticidade;
    }

    public void setCriticidade(CriticalityLevel criticidade) {
        this.criticidade = criticidade;
    }

    public Integer getEngagementScore() {
        return engagementScore;
    }

    public void setEngagementScore(Integer engagementScore) {
        this.engagementScore = engagementScore;
    }

    public Integer getTotalAppointments() {
        return totalAppointments;
    }

    public void setTotalAppointments(Integer totalAppointments) {
        this.totalAppointments = totalAppointments;
    }

    public Integer getMissedAppointments() {
        return missedAppointments;
    }

    public void setMissedAppointments(Integer missedAppointments) {
        this.missedAppointments = missedAppointments;
    }

    public Integer getCancelledAppointments() {
        return cancelledAppointments;
    }

    public void setCancelledAppointments(Integer cancelledAppointments) {
        this.cancelledAppointments = cancelledAppointments;
    }
}