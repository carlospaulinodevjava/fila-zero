package com.filazero.appointmentservice.service;

import com.filazero.appointmentservice.enums.AppointmentStatus;
import com.filazero.appointmentservice.enums.NotificationStatus;
import com.filazero.appointmentservice.enums.NotificationType;
import com.filazero.appointmentservice.exception.ExpiredTokenException;
import com.filazero.appointmentservice.persistence.entity.Appointment;
import com.filazero.appointmentservice.persistence.entity.Notification;
import com.filazero.appointmentservice.persistence.repository.AppointmentRepository;
import com.filazero.appointmentservice.persistence.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class NotificationService {

    private static final int TOKEN_EXPIRATION_HOURS = 48;

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final AppointmentRepository appointmentRepository;

    public NotificationService(NotificationRepository notificationRepository, EmailService emailService, AppointmentRepository appointmentRepository) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public Notification createConfirmationNotification(Long id) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Erro ao achar a consulta"));
        Notification notification = createNotification(appointment, NotificationType.CONFIRMACAO_5_DIAS);
        emailService.sendConfirmationEmail(notification);
        return notification;
    }

    @Transactional
    public Notification createRescheduleNotification(Appointment appointment) {
        return createNotification(appointment, NotificationType.REALOCACAO);
    }

    @Transactional
    public Notification processConfirmation(String trackingToken) {
        Notification notification = validateAndGetNotification(trackingToken);
        Appointment appointment = notification.getAppointment();
        Appointment vagaAberta = null;

        if(Objects.nonNull(appointment.getOfferedSlotAppointmentId())){
            vagaAberta = appointmentRepository.findById(appointment.getOfferedSlotAppointmentId()).orElseThrow();
            if(vagaAberta.getStatus() != AppointmentStatus.VAGA_ABERTA){
                throw new IllegalStateException("Esta vaga já foi realocada para outro paciente. " +
                        "Você pode entrar na fila de espera para receber novas ofertas.");
            }
        }

        if (appointment.getStatus() != AppointmentStatus.PENDENTE_CONFIRMACAO &&
                appointment.getStatus() != AppointmentStatus.REMARCACAO_OFERECIDA && appointment.getStatus() != AppointmentStatus.VAGA_ABERTA) {
            throw new IllegalStateException("Esta vaga já foi realocada para outro paciente. " +
                    "Você pode entrar na fila de espera para receber novas ofertas.");
        }

        notification.setRespondedAt(LocalDateTime.now());
        notification.setStatus(NotificationStatus.RESPONDIDO);
        appointment.setStatus(AppointmentStatus.CONFIRMADO);
        appointment.setUpdatedAt(LocalDateTime.now());


        appointmentRepository.save(appointment);

        if(Objects.nonNull(appointment.getOfferedSlotAppointmentId())){
            vagaAberta.setStatus(AppointmentStatus.REMARCACAO_CONFIRMADA);
            appointmentRepository.save(vagaAberta);
        }

        expiresToken(notification);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification processCancellation(String trackingToken) {
        return processCancellation(trackingToken, null);
    }

    @Transactional
    public Notification processCancellation(String trackingToken, String reason) {
        Notification notification = validateAndGetNotification(trackingToken);
        Appointment appointment = notification.getAppointment();

        if (appointment.getStatus() != AppointmentStatus.PENDENTE_CONFIRMACAO &&
                appointment.getStatus() != AppointmentStatus.REMARCACAO_OFERECIDA) {
            throw new IllegalStateException("Esta consulta já foi cancelada ou realocada.");
        }

        notification.setRespondedAt(LocalDateTime.now());
        notification.setStatus(NotificationStatus.CANCELADO);
        appointment.setStatus(AppointmentStatus.CANCELADO_PELO_PACIENTE);
        appointment.setUpdatedAt(LocalDateTime.now());
        notification.setExpiresAt(LocalDateTime.now());
        if (reason != null && !reason.trim().isEmpty()) {
            String currentNotes = appointment.getNotes() != null ? appointment.getNotes() : "";
            appointment.setNotes(currentNotes + "\nMotivo do cancelamento: " + reason);
        }

        appointmentRepository.save(appointment);

        emailService.sendCancellationConfirmationEmail(notification, reason);
        expiresToken(notification);
        return notificationRepository.save(notification);
    }

    private Notification createNotification(Appointment appointment, NotificationType type) {
        Notification notification = new Notification();
        notification.setAppointment(appointment);
        notification.setPatient(appointment.getPatient());
        notification.setTrackingToken(UUID.randomUUID().toString());
        notification.setType(type);
        notification.setStatus(NotificationStatus.ENVIADO);
        notification.setSentAt(LocalDateTime.now());
        notification.setExpiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS));
        notification.setExpired(false);

        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification createRescheduleNotificationAndSend(Appointment offeredAppointment, LocalDateTime expiresAt) {
        Notification notification = createNotificationCustomExpiry(offeredAppointment, NotificationType.REALOCACAO, expiresAt);

        emailService.sendRescheduleOfferEmail(notification);

        return notification;
    }

    private Notification createNotificationCustomExpiry(Appointment appointment, NotificationType type, LocalDateTime expiresAt) {
        Notification notification = new Notification();
        notification.setAppointment(appointment);
        notification.setPatient(appointment.getPatient());
        notification.setTrackingToken(UUID.randomUUID().toString());
        notification.setType(type);
        notification.setStatus(NotificationStatus.ENVIADO);
        notification.setSentAt(LocalDateTime.now());
        notification.setExpiresAt(expiresAt);
        notification.setExpired(false);

        return notificationRepository.save(notification);
    }

    private Notification validateAndGetNotification(String trackingToken) {
        Notification notification = notificationRepository.findByTrackingToken(trackingToken)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido ou inexistente"));

        if (isExpired(notification) && !notification.getStatus().equals(NotificationStatus.RESPONDIDO)) {
            notification.setExpired(true);
            throw new ExpiredTokenException("Token expirado");
        }

        if (notification.getStatus() == NotificationStatus.RESPONDIDO) {
            throw new IllegalStateException("Notificação já respondida");
        }

        return notification;
    }

    private boolean isExpired(Notification notification) {
        return notification.getExpiresAt() != null
                && LocalDateTime.now().isAfter(notification.getExpiresAt());
    }

    private void expiresToken(Notification notification){
        notification.setExpiresAt(LocalDateTime.now());
        notification.setExpired(true);
    }

}
