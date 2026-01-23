package com.filazero.appointmentservice.dto;

public record AppointmentFilterInput(
        String patientDocument,
        String doctorCrm,
        Boolean futureOnly
) {}
