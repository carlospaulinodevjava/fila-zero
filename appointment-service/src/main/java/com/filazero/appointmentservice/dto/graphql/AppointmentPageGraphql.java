package com.filazero.appointmentservice.dto.graphql;

import com.filazero.appointmentservice.pagination.PageOutput;
import com.filazero.appointmentservice.persistence.entity.Appointment;

import java.util.List;

public record AppointmentPageGraphql(List<Appointment> content, PageOutput pageInfo) {
}
