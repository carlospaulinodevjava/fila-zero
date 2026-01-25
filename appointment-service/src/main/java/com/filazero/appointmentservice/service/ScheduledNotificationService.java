package com.filazero.appointmentservice.service;

import com.filazero.appointmentservice.enums.AppointmentStatus;
import com.filazero.appointmentservice.enums.NotificationStatus;
import com.filazero.appointmentservice.persistence.entity.Appointment;
import com.filazero.appointmentservice.persistence.entity.Notification;
import com.filazero.appointmentservice.persistence.repository.AppointmentRepository;
import com.filazero.appointmentservice.persistence.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ScheduledNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledNotificationService.class);
    private static final int DAYS_BEFORE_APPOINTMENT = 5;
    private static final int CONFIRMATION_DEADLINE_HOURS = 48;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final PatientScoreService patientScoreService;
    private final WaitingQueueService waitingQueueService;

    public ScheduledNotificationService(AppointmentRepository appointmentRepository,
                                       NotificationRepository notificationRepository,
                                       NotificationService notificationService,
                                       PatientScoreService patientScoreService,
                                       WaitingQueueService waitingQueueService) {
        this.appointmentRepository = appointmentRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
        this.patientScoreService = patientScoreService;
        this.waitingQueueService = waitingQueueService;
    }

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void sendConfirmationNotifications() {
        logger.info("=== Iniciando job de envio de notificações de confirmação ===");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetDate = now.plusDays(DAYS_BEFORE_APPOINTMENT);
        
        LocalDateTime startOfDay = targetDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = targetDate.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        
        logger.info("Buscando agendamentos para o dia: {} (entre {} e {})", 
                   targetDate.toLocalDate(), 
                   startOfDay.format(formatter), 
                   endOfDay.format(formatter));

        List<Appointment> appointments = appointmentRepository.findAppointmentsNeedingConfirmation(
            AppointmentStatus.AGENDADO,
            startOfDay,
            endOfDay
        );

        logger.info("Encontrados {} agendamentos que precisam de notificação", appointments.size());

        int successCount = 0;
        int errorCount = 0;

        for (Appointment appointment : appointments) {
            try {
                processAppointmentNotification(appointment);
                successCount++;
            } catch (Exception e) {
                errorCount++;
                logger.error("Erro ao processar notificação para agendamento ID {}: {}", 
                           appointment.getId(), e.getMessage(), e);
            }
        }

        logger.info("=== Job finalizado: {} notificações enviadas com sucesso, {} erros ===", 
                   successCount, errorCount);
    }

    private void processAppointmentNotification(Appointment appointment) {
        logger.info("Processando agendamento ID {}: Paciente '{}', Médico '{}', Data: {}", 
                   appointment.getId(),
                   appointment.getPatient().getName(),
                   appointment.getDoctor().getName(),
                   appointment.getAppointmentDate().format(formatter));

        LocalDateTime confirmationDeadline = appointment.getAppointmentDate()
            .minusHours(CONFIRMATION_DEADLINE_HOURS);
        
        appointment.setStatus(AppointmentStatus.PENDENTE_CONFIRMACAO);
        appointment.setSentAt(LocalDateTime.now());
        appointment.setConfirmationDeadline(confirmationDeadline);
        appointment.setUpdatedAt(LocalDateTime.now());
        
        appointmentRepository.save(appointment);
        
        notificationService.createConfirmationNotification(appointment.getId());
        
        logger.info("Notificação criada com sucesso para agendamento ID {}. Prazo de confirmação: {}", 
                   appointment.getId(), 
                   confirmationDeadline.format(formatter));
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void processExpiredNotifications() {
        logger.info("=== Iniciando job de processamento de notificações expiradas ===");
        
        LocalDateTime now = LocalDateTime.now();
        
        List<Notification> expiredNotifications = notificationRepository.findExpiredNotifications(
            NotificationStatus.ENVIADO,
            now
        );

        logger.info("Encontradas {} notificações expiradas para processar", expiredNotifications.size());

        int processedCount = 0;
        int errorCount = 0;

        for (Notification notification : expiredNotifications) {
            try {
                processExpiredNotification(notification);
                processedCount++;
            } catch (Exception e) {
                errorCount++;
                logger.error("Erro ao processar notificação expirada ID {}: {}", 
                           notification.getId(), e.getMessage(), e);
            }
        }

        logger.info("=== Job finalizado: {} notificações processadas, {} erros ===", 
                   processedCount, errorCount);
    }

    private void processExpiredNotification(Notification notification) {
        Appointment appointment = notification.getAppointment();
        
        logger.info("Processando notificação expirada ID {}: Agendamento ID {}, Paciente '{}', Data: {}", 
                   notification.getId(),
                   appointment.getId(),
                   appointment.getPatient().getName(),
                   appointment.getAppointmentDate().format(formatter));

        notification.setExpired(true);
        notificationRepository.save(notification);

        if (appointment.getStatus() == AppointmentStatus.PENDENTE_CONFIRMACAO ||
            appointment.getStatus() == AppointmentStatus.REMARCACAO_OFERECIDA) {
            
            logger.info("Marcando agendamento ID {} como não confirmado e iniciando realocação", 
                       appointment.getId());
            
            appointment.setStatus(AppointmentStatus.CANCELADO_POR_INCONFIRMACAO);
            appointment.setUpdatedAt(LocalDateTime.now());
            appointmentRepository.save(appointment);

            patientScoreService.updateScore(appointment.getPatient().getId(), 
                                          AppointmentStatus.CANCELADO_POR_INCONFIRMACAO);

            reallocateAppointment(appointment);
            
            logger.info("Agendamento ID {} processado com sucesso. Status atualizado e realocação iniciada.", 
                       appointment.getId());
        } else {
            logger.info("Agendamento ID {} não está em estado que requer realocação. Status atual: {}", 
                       appointment.getId(), appointment.getStatus());
        }
    }

    private void reallocateAppointment(Appointment cancelledAppointment) {
        int attempts = cancelledAppointment.getReallocationAttempts() != null ? 
            cancelledAppointment.getReallocationAttempts() : 0;
        
        if (attempts >= 3) {
            logger.warn("Agendamento ID {} atingiu limite de 3 tentativas de realocação. Marcando como VAGA_ABERTA.", 
                       cancelledAppointment.getId());
            cancelledAppointment.setStatus(AppointmentStatus.VAGA_ABERTA);
            cancelledAppointment.setUpdatedAt(LocalDateTime.now());
            appointmentRepository.save(cancelledAppointment);
            return;
        }

        var nextInQueue = waitingQueueService.findNextInQueue(
            cancelledAppointment.getDoctor().getId()
        );

        if (nextInQueue.isPresent()) {
            var queueEntry = nextInQueue.get();
            
            logger.info("Realocando vaga para próximo paciente na fila: '{}' (Tentativa {}/3)", 
                       queueEntry.getPatient().getName(), attempts + 1);
            
            Appointment newAppointment = new Appointment();
            newAppointment.setPatient(queueEntry.getPatient());
            newAppointment.setDoctor(cancelledAppointment.getDoctor());
            newAppointment.setNurse(cancelledAppointment.getNurse());
            newAppointment.setAppointmentDate(cancelledAppointment.getAppointmentDate());
            newAppointment.setStatus(AppointmentStatus.REMARCACAO_OFERECIDA);
            newAppointment.setCreatedAt(LocalDateTime.now());
            newAppointment.setReallocationAttempts(attempts + 1);
            appointmentRepository.save(newAppointment);

            waitingQueueService.markAsScheduled(queueEntry.getId());

            notificationService.createRescheduleNotification(newAppointment);
            
            logger.info("Nova notificação de realocação criada para agendamento ID {}", 
                       newAppointment.getId());
        } else {
            logger.warn("Não há pacientes na fila para realocação. Marcando agendamento ID {} como VAGA_ABERTA.", 
                       cancelledAppointment.getId());
            cancelledAppointment.setStatus(AppointmentStatus.VAGA_ABERTA);
            cancelledAppointment.setUpdatedAt(LocalDateTime.now());
            appointmentRepository.save(cancelledAppointment);
        }
    }
}
