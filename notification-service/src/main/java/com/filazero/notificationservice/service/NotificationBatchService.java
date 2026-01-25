package com.filazero.notificationservice.service;

import com.filazero.notificationservice.enums.AppointmentStatus;
import com.filazero.notificationservice.enums.NotificationStatus;
import com.filazero.notificationservice.enums.NotificationType;
import com.filazero.notificationservice.persistence.entity.Appointment;
import com.filazero.notificationservice.persistence.entity.Notification;
import com.filazero.notificationservice.persistence.repository.AppointmentRepository;
import com.filazero.notificationservice.persistence.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationBatchService {

    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;

    public NotificationBatchService(AppointmentRepository appointmentRepository,
                                    NotificationRepository notificationRepository) {
        this.appointmentRepository = appointmentRepository;
        this.notificationRepository = notificationRepository;
    }

    @Scheduled(cron = "0 0 9 * * *") // todos os dias às 09:00
    public void executarNotificacoesProximasConsultas() {
        notificarConsultasProximas(7, 48);
    }

    @Transactional
    public int notificarConsultasProximas(int diasAntecedencia, int horasPrazoConfirmacao) {

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime dataLimiteBusca = agora.plusDays(diasAntecedencia);

        // 1) Buscar consultas elegíveis
        List<Appointment> appointments =
                appointmentRepository.buscarConsultasProximasNaoNotificadas(
                        agora,
                        dataLimiteBusca,
                        NotificationType.CONFIRMACAO_15_DIAS,
                        AppointmentStatus.AGUARDANDO_NOTIFICACAO

                );
        int totalNotificadas = 0;

        // 2) Processar cada consulta
        for (Appointment appointment : appointments) {

            // Define o prazo máximo para confirmação
            LocalDateTime prazoConfirmacao = agora.plusHours(horasPrazoConfirmacao);

            // 3) Atualiza a consulta
            appointment.setStatus(AppointmentStatus.PENDENTE_CONFIRMACAO);
            appointment.setSentAt(agora);
            appointment.setConfirmationDeadline(prazoConfirmacao);

            // 4) Cria a notificação
            Notification notificacao = new Notification();
            notificacao.setTrackingToken(UUID.randomUUID().toString());
            notificacao.setAppointment(appointment);
            notificacao.setPatient(appointment.getPatient());
            notificacao.setType(NotificationType.CONFIRMACAO_15_DIAS);
            notificacao.setStatus(NotificationStatus.ENVIADO);
            notificacao.setSentAt(agora);
            notificacao.setExpiresAt(prazoConfirmacao);
            notificacao.setExpired(false);

            notificationRepository.save(notificacao);
            totalNotificadas++;
        }

        return totalNotificadas;
    }
}
