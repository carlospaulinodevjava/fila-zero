package com.filazero.appointmentservice.service.validator;

import com.filazero.appointmentservice.enums.AppointmentStatus;
import com.filazero.appointmentservice.persistence.entity.Appointment;
import com.filazero.appointmentservice.persistence.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AppointmentValidationService {

    private final AppointmentRepository appointmentRepository;

    private static final int BUSINESS_HOUR_START = 8;
    private static final int BUSINESS_HOUR_END = 18;

    public AppointmentValidationService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public List<String> validateAppointment(Long patientId, Long doctorId, LocalDateTime appointmentDate) {
        List<String> errors = new ArrayList<>();

        if (isPastDate(appointmentDate)) {
            errors.add("Não é possível agendar consultas no passado");
        }

        if (!isBusinessHours(appointmentDate)) {
            errors.add("Horário fora do expediente. Horário permitido: 08:00 às 18:00, Segunda a Sexta");
        }

        if (hasDoctorConflict(doctorId, appointmentDate)) {
            errors.add("Médico já possui agendamento neste horário");
        }

        if (hasPatientConflict(patientId, appointmentDate)) {
            errors.add("Paciente já possui agendamento neste horário");
        }

        return errors;
    }

    private boolean isPastDate(LocalDateTime appointmentDate) {
        return appointmentDate.isBefore(LocalDateTime.now());
    }

    private boolean isBusinessHours(LocalDateTime appointmentDate) {
        DayOfWeek dayOfWeek = appointmentDate.getDayOfWeek();
        int hour = appointmentDate.getHour();

        boolean isWeekday = dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
        boolean isBusinessHour = hour >= BUSINESS_HOUR_START && hour < BUSINESS_HOUR_END;

        return isWeekday && isBusinessHour;
    }

    private boolean hasDoctorConflict(Long doctorId, LocalDateTime appointmentDate) {
        LocalDateTime startWindow = appointmentDate.minusMinutes(30);
        LocalDateTime endWindow = appointmentDate.plusMinutes(30);

        List<Appointment> doctorAppointments = appointmentRepository.findAll().stream()
            .filter(a -> a.getDoctor().getId().equals(doctorId))
            .filter(a -> a.getStatus() != AppointmentStatus.CANCELADO_PELO_PACIENTE)
            .filter(a -> a.getStatus() != AppointmentStatus.CANCELADO_POR_INCONFIRMACAO)
            .filter(a -> a.getAppointmentDate().isAfter(startWindow) && 
                        a.getAppointmentDate().isBefore(endWindow))
            .toList();

        return !doctorAppointments.isEmpty();
    }

    private boolean hasPatientConflict(Long patientId, LocalDateTime appointmentDate) {
        LocalDateTime startWindow = appointmentDate.minusMinutes(30);
        LocalDateTime endWindow = appointmentDate.plusMinutes(30);

        List<Appointment> patientAppointments = appointmentRepository.findAll().stream()
            .filter(a -> a.getPatient().getId().equals(patientId))
            .filter(a -> a.getStatus() != AppointmentStatus.CANCELADO_PELO_PACIENTE)
            .filter(a -> a.getStatus() != AppointmentStatus.CANCELADO_POR_INCONFIRMACAO)
            .filter(a -> a.getAppointmentDate().isAfter(startWindow) && 
                        a.getAppointmentDate().isBefore(endWindow))
            .toList();

        return !patientAppointments.isEmpty();
    }
}
