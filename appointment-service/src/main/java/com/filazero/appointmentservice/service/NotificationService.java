package com.filazero.appointmentservice.service;

import com.filazero.appointmentservice.enums.AppointmentStatus;
import com.filazero.appointmentservice.enums.NotificationStatus;
import com.filazero.appointmentservice.enums.NotificationType;
import com.filazero.appointmentservice.persistence.entity.Appointment;
import com.filazero.appointmentservice.persistence.entity.Notification;
import com.filazero.appointmentservice.persistence.repository.AppointmentRepository;
import com.filazero.appointmentservice.persistence.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        notification.setRespondedAt(LocalDateTime.now());
        notification.setStatus(NotificationStatus.RESPONDIDO);
        notification.getAppointment().setStatus(AppointmentStatus.CONFIRMADO);

        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification processCancellation(String trackingToken) {
        Notification notification = validateAndGetNotification(trackingToken);

        notification.setRespondedAt(LocalDateTime.now());
        notification.setStatus(NotificationStatus.RESPONDIDO);
        // TODO validar se esse cancelamento deve ser feito pelo paciente
        notification.getAppointment().setStatus(AppointmentStatus.CANCELADO_PELO_PACIENTE);

        return notificationRepository.save(notification);
    }

    /* =========================
       Métodos auxiliares
       ========================= */

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
        // notification.setMessage(buildMessage(notification));
        // notification.setChannel(NotificationChannel.EMAIL);

        return notificationRepository.save(notification);
    }

    private Notification validateAndGetNotification(String trackingToken) {
        Notification notification = notificationRepository.findByTrackingToken(trackingToken)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido ou inexistente"));

        if (notification.getStatus() == NotificationStatus.RESPONDIDO) {
            throw new IllegalStateException("Notificação já respondida");
        }

        if (isExpired(notification)) {
            notification.setExpired(true);
            throw new IllegalStateException("Token expirado");
        }

        return notification;
    }

    private boolean isExpired(Notification notification) {
        return notification.getExpiresAt() != null
                && LocalDateTime.now().isAfter(notification.getExpiresAt());
    }

}
