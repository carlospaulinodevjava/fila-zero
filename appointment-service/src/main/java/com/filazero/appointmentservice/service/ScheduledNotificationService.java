package com.filazero.appointmentservice.service;

import com.filazero.appointmentservice.enums.AppointmentStatus;
import com.filazero.appointmentservice.enums.NotificationStatus;
import com.filazero.appointmentservice.enums.NotificationType;
import com.filazero.appointmentservice.persistence.entity.Appointment;
import com.filazero.appointmentservice.persistence.entity.Notification;
import com.filazero.appointmentservice.persistence.repository.AppointmentRepository;
import com.filazero.appointmentservice.persistence.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ScheduledNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledNotificationService.class);
    private static final int DAYS_BEFORE_APPOINTMENT = 5;
    private static final int CONFIRMATION_DEADLINE_HOURS = 48;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final int MIN_DAYS_TO_BE_CANDIDATE = 14;     // só candidatos com consulta >= 14 dias
    private static final int REALOCACAO_EXPIRES_HOURS = 24;     // prazo para responder a oferta

    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public ScheduledNotificationService(AppointmentRepository appointmentRepository,
                                        NotificationRepository notificationRepository,
                                        NotificationService notificationService) {
        this.appointmentRepository = appointmentRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    //@Scheduled(cron = "0 0 8 * * *")
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

    // @Scheduled(cron = "0 */10 * * * *") // a cada 10 minutos
    @Transactional
    public void processExpiredConfirmations() {
        logger.info("=== Iniciando job de processamento de confirmações expiradas ===");

        LocalDateTime now = LocalDateTime.now();

        List<Notification> expiredNotifications = notificationRepository.findExpiredOrCancelledNotifications(
                NotificationStatus.ENVIADO, NotificationStatus.CANCELADO,
                now
        );

        logger.info("Encontradas {} notificações expiradas para processar", expiredNotifications.size());

        int processedCount = 0;
        int errorCount = 0;

        for (Notification notification : expiredNotifications) {
            try {
                processExpiredConfirmationNotification(notification, now);
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

    private void processExpiredConfirmationNotification(Notification notification, LocalDateTime now) {
        Appointment appointment = notification.getAppointment();

        logger.info("Processando notificação expirada ID {}: Agendamento ID {}, Paciente '{}', Data: {}",
                notification.getId(),
                appointment.getId(),
                appointment.getPatient().getName(),
                appointment.getAppointmentDate().format(formatter));

        // expirar a notificacao
        notification.setExpired(true);
        notificationRepository.save(notification);

        //  abrir vaga para appointments nao confirmados, seja da notificacao inicial ou de remarcacao oferecida
        if (appointment.getStatus() == AppointmentStatus.CANCELADO_PELO_PACIENTE ||
                appointment.getStatus() == AppointmentStatus.REMARCACAO_OFERECIDA || appointment.getStatus() == AppointmentStatus.AGENDADO) {

            logger.info("Marcando agendamento ID {} como VAGA_ABERTA (expiração). Status anterior: {}",
                    appointment.getId(), appointment.getStatus());

            appointment.setStatus(AppointmentStatus.VAGA_ABERTA);
            appointment.setUpdatedAt(now);

            appointmentRepository.save(appointment);

            logger.info("Agendamento ID {} atualizado para VAGA_ABERTA com sucesso.", appointment.getId());
        } else {
            logger.info("Agendamento ID {} não está em estado que requer abertura de vaga. Status atual: {}",
                    appointment.getId(), appointment.getStatus());
        }
    }

    //@Scheduled(cron = "0 */10 * * * *") // a cada 10 minuto
    @Transactional
    public void processOpenSlotsAndOfferReallocation() {
        logger.info("=== Iniciando job de realocação: preenchimento de vagas abertas ===");

        LocalDateTime now = LocalDateTime.now();
        List<Appointment> openSlots = appointmentRepository.buscarVagasAbertas(now);

        logger.info("Encontradas {} vagas abertas para tentar preencher", openSlots.size());

        int offersCreated = 0;
        int errorCount = 0;

        for (Appointment slot : openSlots) {
            try {
                boolean created = offerSlotToBestCandidate(slot, now);
                if (created) offersCreated++;
            } catch (Exception e) {
                errorCount++;
                logger.error("Erro ao processar realocação para vaga ID {}: {}",
                        slot.getId(), e.getMessage(), e);
            }
        }

        logger.info("=== Job finalizado: {} ofertas de realocação criadas, {} erros ===",
                offersCreated, errorCount);
    }

    private boolean offerSlotToBestCandidate(Appointment slot, LocalDateTime now) {
        logger.info("Processando vaga aberta ID {}: Paciente atual '{}', Médico '{}', Data: {}",
                slot.getId(),
                slot.getPatient() != null ? slot.getPatient().getName() : "(sem paciente)",
                slot.getDoctor().getName(),
                slot.getAppointmentDate().format(formatter));

        // Só buscar candidatos com consulta marcada para >= 14 dias
        LocalDateTime minCandidateDate = now.plusDays(MIN_DAYS_TO_BE_CANDIDATE);

        List<Appointment> candidates = appointmentRepository.buscarCandidatosElegiveisParaAntecipacao(
                slot.getDoctor().getId(),
                minCandidateDate,
                now
        );

        logger.info("Encontrados {} candidatos elegíveis para a vaga ID {}", candidates.size(), slot.getId());

        if (candidates.isEmpty()) {
            logger.info("Nenhum candidato elegível encontrado para vaga ID {}. Mantendo como VAGA_ABERTA.", slot.getId());
            return false;
        }

        Appointment best = candidates.stream()
                .max(Comparator.comparingInt(a -> calcularScoreParaAntecipacao(a, now)))
                .orElseThrow();

        logger.info("Melhor candidato selecionado: Appointment ID {}, Paciente '{}', Data original: {}",
                best.getId(),
                best.getPatient().getName(),
                best.getAppointmentDate().format(formatter));

        // Cria o appointment com status REMARCACAO_OFERECIDA. Sera utilizado para tentar antecipar a consulta
        Appointment offered = new Appointment();
        offered.setPatient(best.getPatient());
        offered.setDoctor(slot.getDoctor());
        offered.setNurse(slot.getNurse());
        offered.setAppointmentDate(slot.getAppointmentDate());
        offered.setStatus(AppointmentStatus.REMARCACAO_OFERECIDA);
        offered.setCreatedAt(now);
        offered.setUpdatedAt(now);
        offered.setCriticidade(best.getCriticidade());
        offered.setOfferedSlotAppointmentId(slot.getId());
        offered.setNotes("Oferta de antecipação gerada automaticamente. Consulta original do paciente: "
                + best.getAppointmentDate().format(formatter));

        appointmentRepository.save(offered);

        // Cria a notificacao de realocacao e envia email.
        LocalDateTime expiresAt = now.plusHours(REALOCACAO_EXPIRES_HOURS);

        try {
            notificationService.createRescheduleNotificationAndSend(offered, expiresAt);

            logger.info("Oferta criada com sucesso: vaga ID {} -> novo appointment ID {} (Paciente '{}'). Expira em {}",
                    slot.getId(),
                    offered.getId(),
                    offered.getPatient().getName(),
                    expiresAt.format(formatter));

            return true;

        } catch (Exception e) {
            logger.error("Erro ao enviar oferta de realocação para vaga ID {} / appointment oferecido ID {}: {}",
                    slot.getId(), offered.getId(), e.getMessage(), e);

            String currentNotes = slot.getNotes() != null ? slot.getNotes() : "";
            slot.setNotes(currentNotes + "\nFalha ao enviar oferta de realocação em "
                    + now.format(formatter) + ": " + e.getMessage());
            slot.setUpdatedAt(now);
            appointmentRepository.save(slot);

            return false;
        }
    }

    private int calcularScoreParaAntecipacao(Appointment appointment, LocalDateTime now) {
        int pesoCriticidade = (appointment.getCriticidade() != null) ? appointment.getCriticidade().getPeso() : 2; // NORMAL

        long diasEspera = 0;
        if (appointment.getCreatedAt() != null) {
            diasEspera = Duration.between(appointment.getCreatedAt(), now).toDays();
        }

        return (pesoCriticidade * 1000) + (int) (diasEspera * 10);
    }
}
