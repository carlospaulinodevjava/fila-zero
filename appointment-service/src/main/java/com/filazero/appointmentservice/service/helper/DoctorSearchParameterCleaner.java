package com.filazero.appointmentservice.service.helper;

import com.filazero.appointmentservice.dto.DoctorSearchParameters;
import org.springframework.stereotype.Component;

@Component
public class DoctorSearchParameterCleaner {

    public DoctorSearchParameters cleanSearchParameters(String name, String specialty, String crm) {
        return new DoctorSearchParameters(
                cleanParameter(name),
                cleanParameter(specialty),
                cleanParameter(crm)
        );
    }

    private String cleanParameter(String parameter) {
        return (parameter != null && !parameter.trim().isEmpty()) ? parameter.trim() : null;
    }

    public boolean hasValidParameters(DoctorSearchParameters parameters) {
        return parameters.name() != null ||
                parameters.specialty() != null ||
                parameters.crm() != null;
    }
}
