package com.filazero.appointmentservice.dto.graphql;

public record AppointmentFilterInput(
        String patientDocument,
        String doctorCrm,
        Boolean futureOnly
) {}
