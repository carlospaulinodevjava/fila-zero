package com.filazero.notificationservice.service;

import com.filazero.notificationservice.enums.AppointmentStatus;
import com.filazero.notificationservice.enums.NotificationStatus;
import com.filazero.notificationservice.enums.NotificationType;
import com.filazero.notificationservice.persistence.entity.Appointment;
import com.filazero.notificationservice.persistence.entity.Notification;
import com.filazero.notificationservice.persistence.entity.Patient;
import com.filazero.notificationservice.persistence.repository.AppointmentRepository;
import com.filazero.notificationservice.persistence.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
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
                        NotificationType.CONFIRMACAO_5_DIAS,
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
            Notification notification = new Notification();
            notification.setAppointment(appointment);
            notification.setPatient(appointment.getPatient());
            notification.setTrackingToken(UUID.randomUUID().toString());
            notification.setType(NotificationType.CONFIRMACAO_5_DIAS);
            notification.setStatus(NotificationStatus.ENVIADO);
            notification.setSentAt(LocalDateTime.now());
            notification.setExpiresAt(LocalDateTime.now().plusHours(horasPrazoConfirmacao));
            notification.setExpired(false);

            notificationRepository.save(notification);
            totalNotificadas++;
        }

        return totalNotificadas;
    }

    @Scheduled(cron = "0 */5 * * * *") // a cada 5 minutos
    public void agendarExpiracaoConfirmacoes() {
        expirarConfirmacoesPendentes();
    }

    @Transactional
    public int expirarConfirmacoesPendentes() {

        LocalDateTime agora = LocalDateTime.now();

        List<Appointment> vencidas = appointmentRepository.buscarAppointmentsNaoConfirmados(
                AppointmentStatus.PENDENTE_CONFIRMACAO,
                agora
        );

        int total = 0;

        for (Appointment a : vencidas) {
            a.setStatus(AppointmentStatus.VAGA_ABERTA);
            a.setUpdatedAt(agora);

            if (a.getNotes() == null || a.getNotes().isBlank()) {
                a.setNotes("Cancelado por inconfirmação (prazo expirado em " + a.getConfirmationDeadline() + ")");
            } else {
                a.setNotes(a.getNotes() + "\nCancelado por inconfirmação (prazo expirado em " + a.getConfirmationDeadline() + ")");
            }

            total++;
        }

        return total;
    }

    @Scheduled(cron = "0 */10 * * * *") // a cada 10 minutos
    public void agendarOfertaAntecipacao() {
        oferecerAntecipacaoParaVagasAbertas(14, 24, 50);
    }

    @Transactional
    public int oferecerAntecipacaoParaVagasAbertas(int diasMinimosParaSerCandidato,
                                                   int prazoHorasParaResponder,
                                                   int limiteCandidatosPorVaga) {

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime dataMinimaCandidato = agora.plusDays(diasMinimosParaSerCandidato);

        List<Appointment> vagasAbertas = appointmentRepository.buscarVagasAbertas(agora);

        int totalOfertas = 0;

        for (Appointment vaga : vagasAbertas) {

            // 1) Buscar candidatos do mesmo médico, limitando quantidade
            List<Appointment> candidatos = appointmentRepository.buscarCandidatosElegiveisParaAntecipacao(
                    vaga.getDoctor().getId(),
                    dataMinimaCandidato,
                    agora,
                    PageRequest.of(0, limiteCandidatosPorVaga)
            );

            if (candidatos.isEmpty()) {
                continue;
            }

            // 2) Escolher o melhor candidato pelo score
            Appointment melhorCandidato = escolherMelhorCandidato(candidatos, agora);

            // 3) Registrar oferta no appointment do candidato
            LocalDateTime expiraEm = agora.plusHours(prazoHorasParaResponder);

            melhorCandidato.setStatus(AppointmentStatus.REMARCACAO_OFERECIDA);
            melhorCandidato.setOfferedSlotAppointmentId(vaga.getId());
            melhorCandidato.setOfferExpiresAt(expiraEm);
            melhorCandidato.setUpdatedAt(agora);

            // 4) Criar notification REALOCACAO com token
            Notification notificacao = new Notification();
            notificacao.setTrackingToken(UUID.randomUUID().toString());
            notificacao.setAppointment(melhorCandidato);
            notificacao.setPatient(melhorCandidato.getPatient());
            notificacao.setType(NotificationType.REALOCACAO);
            notificacao.setStatus(NotificationStatus.ENVIADO);
            notificacao.setSentAt(agora);
            notificacao.setExpiresAt(expiraEm);
            notificacao.setExpired(false);

            notificationRepository.save(notificacao);

            totalOfertas++;
        }

        return totalOfertas;
    }

    private Appointment escolherMelhorCandidato(List<Appointment> candidatos, LocalDateTime agora) {
        return candidatos.stream()
                .max(Comparator.comparingInt(a -> calcularScore(a, agora)))
                .orElseThrow();
    }

    private int calcularScore(Appointment candidato, LocalDateTime agora) {

        Patient p = candidato.getPatient();

        int pesoCriticidade = (p.getCriticidade() != null) ? p.getCriticidade().getPeso() : 2;
        long diasEspera = 0;

        if (candidato.getCreatedAt() != null) {
            diasEspera = java.time.Duration.between(candidato.getCreatedAt(), agora).toDays();
        }

        return (pesoCriticidade * 1000) + (int) (diasEspera * 10);
    }
}
