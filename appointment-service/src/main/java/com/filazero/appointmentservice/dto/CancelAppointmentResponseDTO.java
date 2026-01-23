package com.filazero.appointmentservice.dto;

public record CancelAppointmentResponseDTO(
    AppointmentResponseDTO cancelledAppointment,
    QueuePositionResponseDTO nextInQueue,
    AppointmentResponseDTO newAppointment,
    boolean reallocated
) {}
