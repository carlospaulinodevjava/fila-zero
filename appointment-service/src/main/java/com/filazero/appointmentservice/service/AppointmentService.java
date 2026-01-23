package com.filazero.appointmentservice.service;

import com.filazero.appointmentservice.dto.*;
import com.filazero.appointmentservice.exception.DataNotFoundException;
import com.filazero.appointmentservice.persistence.entity.Appointment;
import com.filazero.appointmentservice.persistence.entity.Doctor;
import com.filazero.appointmentservice.persistence.entity.Nurse;
import com.filazero.appointmentservice.persistence.entity.Patient;
import com.filazero.appointmentservice.persistence.repository.AppointmentRepository;
import com.filazero.appointmentservice.persistence.repository.DoctorRepository;
import com.filazero.appointmentservice.persistence.repository.NurseRepository;
import com.filazero.appointmentservice.persistence.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final NurseRepository nurseRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              NurseRepository nurseRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.nurseRepository = nurseRepository;
    }

    public AppointmentResponseDTO create(CreateAppointmentRequestDTO createAppointmentRequestDTO) {
        Appointment appointment = buildAppointment(createAppointmentRequestDTO);
        appointment.setStatus(createAppointmentRequestDTO.status());
        appointmentRepository.save(appointment);
        return toResponseDTO(appointment);
    }

    public Page<AppointmentResponseDTO> getAll(Pageable pageable) {
        return appointmentRepository.findAll(pageable).map(this::toResponseDTO);
    }

    public AppointmentResponseDTO getById(Long id) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Nenhum atendimento encontrado com o ID: " + id));
        return toResponseDTO(appointment);
    }

    public AppointmentResponseDTO update(Long appointmentId, UpdateAppointmentRequestDTO updateRequest) {
        Appointment existingAppointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Nenhum atendimento encontrado com o ID: " + appointmentId));

        patientRepository.findById(updateRequest.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Nenhum paciente encontrado com o ID: " + updateRequest.getPatientId()));
        doctorRepository.findById(updateRequest.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Nenhum médico encontrado com o ID: " + updateRequest.getDoctorId()));
        nurseRepository.findById(updateRequest.getNurseId())
                .orElseThrow(() -> new IllegalArgumentException("Nenhum enfermeiro encontrado com o ID: " + updateRequest.getNurseId()));


        if (updateRequest.getPatientId() != null) {
            existingAppointment.setPatient(findPatientById(updateRequest.getPatientId()));
        }
        if (updateRequest.getDoctorId() != null) {
            existingAppointment.setDoctor(findDoctorById(updateRequest.getDoctorId()));
        }

        if( updateRequest.getNurseId() != null) {
            existingAppointment.setNurse(findNurseById(updateRequest.getNurseId()));
        }

        if (updateRequest.getAppointmentDate() != null) {
            existingAppointment.setAppointmentDate(updateRequest.getAppointmentDate());
        }
        if (updateRequest.getStatus() != null) {
            existingAppointment.setStatus(updateRequest.getStatus());
        }
        if (updateRequest.getNotes() != null) {
            existingAppointment.setNotes(updateRequest.getNotes());
        }
        existingAppointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(existingAppointment);

        return toResponseDTO(existingAppointment);
    }

    public void delete(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nenhum atendimento encontrado com o ID: " + id));
        appointmentRepository.delete(appointment);
    }

    private Appointment buildAppointment(CreateAppointmentRequestDTO dto) {
        Appointment appointment = new Appointment();
        appointment.setPatient(findPatientById(dto.patientId()));
        appointment.setDoctor(findDoctorById(dto.doctorId()));
        appointment.setNurse(dto.nurseId() != null ? findNurseById(dto.nurseId()) : null);
        appointment.setAppointmentDate(dto.appointmentDate());
        appointment.setStatus(dto.status());
        appointment.setNotes(dto.notes());
        appointment.setCreatedAt(LocalDateTime.now());
        return appointment;
    }

    private Patient findPatientById(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Nenhum paciente encontrado com o ID: " + patientId));
    }

    private Doctor findDoctorById(Long doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Nenhum médico encontrado com o ID: " + doctorId));
    }

    private Nurse findNurseById(Long nurseId) {
        return nurseRepository.findById(nurseId)
                .orElseThrow(() -> new IllegalArgumentException("Nenhum enfermeiro encontrado com o ID: " + nurseId));
    }


    private AppointmentResponseDTO toResponseDTO(Appointment appointment) {
        AppointmentResponseDTO dto = new AppointmentResponseDTO();
        dto.setId(appointment.getId());
        dto.setDoctorId(appointment.getDoctor() != null ? appointment.getDoctor().getId() : null);
        dto.setPatientId(appointment.getPatient() != null ? appointment.getPatient().getId() : null);
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setStatus(appointment.getStatus());
        dto.setNotes(appointment.getNotes());
        return dto;
    }

    public boolean isPatientOwner(String username, Long appointmentId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) return false;
        Appointment appointment = appointmentOpt.get();
        Patient patient = appointment.getPatient();
        if (patient == null) return false;
        return username.equalsIgnoreCase(patient.getEmail());
    }
}
