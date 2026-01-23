package com.filazero.appointmentservice.service;

import com.filazero.appointmentservice.dto.*;
import com.filazero.appointmentservice.enums.AppointmentStatus;
import com.filazero.appointmentservice.exception.DataNotFoundException;
import com.filazero.appointmentservice.persistence.entity.Appointment;
import com.filazero.appointmentservice.persistence.entity.Doctor;
import com.filazero.appointmentservice.persistence.entity.Nurse;
import com.filazero.appointmentservice.persistence.entity.Patient;
import com.filazero.appointmentservice.persistence.entity.WaitingQueue;
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
    private final WaitingQueueService waitingQueueService;
    private final PatientScoreService patientScoreService;
    private final NotificationService notificationService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              NurseRepository nurseRepository,
                              WaitingQueueService waitingQueueService,
                              PatientScoreService patientScoreService,
                              NotificationService notificationService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.nurseRepository = nurseRepository;
        this.waitingQueueService = waitingQueueService;
        this.patientScoreService = patientScoreService;
        this.notificationService = notificationService;
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

    public AppointmentResponseDTO confirmAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new DataNotFoundException("Agendamento não encontrado com ID: " + appointmentId));

        appointment.setStatus(AppointmentStatus.CONFIRMADO);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);

        patientScoreService.updateScore(appointment.getPatient().getId(), AppointmentStatus.CONFIRMADO);

        return toResponseDTO(appointment);
    }

    public AppointmentResponseDTO cancelAppointment(Long appointmentId, String reason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new DataNotFoundException("Agendamento não encontrado com ID: " + appointmentId));

        appointment.setStatus(AppointmentStatus.CANCELADO_PELO_PACIENTE);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);

        patientScoreService.updateScore(appointment.getPatient().getId(), AppointmentStatus.CANCELADO_PELO_PACIENTE);

        reallocateAppointment(appointment);

        return toResponseDTO(appointment);
    }

    private void reallocateAppointment(Appointment cancelledAppointment) {
        Optional<WaitingQueue> nextInQueue = waitingQueueService.findNextInQueue(
            cancelledAppointment.getDoctor().getId()
        );

        if (nextInQueue.isPresent()) {
            WaitingQueue queueEntry = nextInQueue.get();
            
            Appointment newAppointment = new Appointment();
            newAppointment.setPatient(queueEntry.getPatient());
            newAppointment.setDoctor(cancelledAppointment.getDoctor());
            newAppointment.setNurse(cancelledAppointment.getNurse());
            newAppointment.setAppointmentDate(cancelledAppointment.getAppointmentDate());
            newAppointment.setStatus(AppointmentStatus.PENDENTE_CONFIRMACAO);
            newAppointment.setCreatedAt(LocalDateTime.now());
            appointmentRepository.save(newAppointment);

            waitingQueueService.markAsScheduled(queueEntry.getId());

            notificationService.createRescheduleNotification(newAppointment);
        }
    }

    public void markAsCompleted(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new DataNotFoundException("Agendamento não encontrado com ID: " + appointmentId));

        appointment.setStatus(AppointmentStatus.REALIZADO);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);

        patientScoreService.updateScore(appointment.getPatient().getId(), AppointmentStatus.REALIZADO);
    }

    public void markAsNoShow(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new DataNotFoundException("Agendamento não encontrado com ID: " + appointmentId));

        appointment.setStatus(AppointmentStatus.CANCELADO_POR_INCONFIRMACAO);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);

        patientScoreService.updateScore(appointment.getPatient().getId(), AppointmentStatus.CANCELADO_POR_INCONFIRMACAO);

        reallocateAppointment(appointment);
    }
}
