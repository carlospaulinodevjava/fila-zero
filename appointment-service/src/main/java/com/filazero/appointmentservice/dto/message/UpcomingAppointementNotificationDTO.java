package com.filazero.appointmentservice.dto.message;

import com.filazero.appointmentservice.persistence.entity.Doctor;
import com.filazero.appointmentservice.persistence.entity.Nurse;
import com.filazero.appointmentservice.persistence.entity.Patient;

import java.time.LocalDateTime;

public record UpcomingAppointementNotificationDTO(Long id,
                                                  Patient patient,
                                                  Doctor doctor,
                                                  Nurse nurse,
                                                  LocalDateTime appointmentDate,
                                                  String status,
                                                  String notes,
                                                  LocalDateTime createdAt,
                                                  LocalDateTime updatedAt) {
}
