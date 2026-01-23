package com.filazero.appointmentservice.service;

import com.filazero.appointmentservice.enums.AppointmentStatus;
import com.filazero.appointmentservice.enums.NotificationStatus;
import com.filazero.appointmentservice.enums.NotificationType;
import com.filazero.appointmentservice.persistence.entity.Appointment;
import com.filazero.appointmentservice.persistence.entity.Notification;
import com.filazero.appointmentservice.persistence.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification createConfirmationNotification(Appointment appointment) {
        Notification notification = new Notification();
        notification.setAppointment(appointment);
        notification.setPatient(appointment.getPatient());
        notification.setTrackingToken(UUID.randomUUID().toString());
        notification.setType(NotificationType.CONFIRMACAO_5_DIAS);
        notification.setStatus(NotificationStatus.ENVIADO);
        notification.setSentAt(LocalDateTime.now());
        notification.setExpiresAt(LocalDateTime.now().plusHours(48));
        notification.setExpired(false);
        //notification.setMessage(buildMessage(notification));
        //notification.setChannel(NotificationChannel.EMAIL);

        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification createRescheduleNotification(Appointment appointment) {
        Notification notification = new Notification();
        notification.setAppointment(appointment);
        notification.setPatient(appointment.getPatient());
        notification.setTrackingToken(UUID.randomUUID().toString());
        notification.setType(NotificationType.REALOCACAO);
        notification.setStatus(NotificationStatus.ENVIADO);
        notification.setSentAt(LocalDateTime.now());
        notification.setExpiresAt(LocalDateTime.now().plusHours(48));
        notification.setExpired(false);
        //notification.setMessage(buildMessage(notification));
        //notification.setChannel(NotificationChannel.EMAIL);

        return notificationRepository.save(notification);
    }


    @Transactional
    public Notification processConfirmation(String trackingToken) {

        Notification notification = notificationRepository.findByTrackingToken(trackingToken)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido ou inexistente"));

        if (notification.getStatus() == NotificationStatus.RESPONDIDO) {
            throw new IllegalStateException("Notificação já respondida");
        }

        if (notification.getExpiresAt() != null && LocalDateTime.now().isAfter(notification.getExpiresAt())) {
            notification.setExpired(true);
            throw new IllegalStateException("Token expirado");
        }

        notification.setRespondedAt(LocalDateTime.now());
        notification.setStatus(NotificationStatus.RESPONDIDO);
        notification.getAppointment().setStatus(AppointmentStatus.CONFIRMADO);
        notificationRepository.save(notification);
        return notification;
    }

    @Transactional
    public Notification processCancellation(String trackingToken) {

        Notification notification = notificationRepository.findByTrackingToken(trackingToken)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido ou inexistente"));

        if (notification.getStatus() == NotificationStatus.RESPONDIDO) {
            throw new IllegalStateException("Notificação já respondida");
        }

        if (notification.getExpiresAt() != null && LocalDateTime.now().isAfter(notification.getExpiresAt())) {
            notification.setExpired(true);
            throw new IllegalStateException("Token expirado");
        }

        notification.setRespondedAt(LocalDateTime.now());
        notification.setStatus(NotificationStatus.RESPONDIDO);
        // TODO Validar se esse cancelamento deve ser feito pelo paciente ou se é outro tipo de cancelamento
        notification.getAppointment().setStatus(AppointmentStatus.CANCELADO_PELO_PACIENTE);
        notificationRepository.save(notification);
        return notification;
    }


}
