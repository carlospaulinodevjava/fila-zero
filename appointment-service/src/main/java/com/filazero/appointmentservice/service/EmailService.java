package com.filazero.appointmentservice.service;

import com.filazero.appointmentservice.persistence.entity.Notification;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.format.DateTimeFormatter;


@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender,
                        SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendConfirmationEmail(Notification notification) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(notification.getPatient().getEmail());
            helper.setSubject("Confirmação de consulta");
            helper.setFrom("filazerofiap@gmail.com");

            Context context = new Context();
            context.setVariable("patientName", notification.getPatient().getName());

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
            context.setVariable(
                    "appointmentDate",
                    notification.getAppointment()
                            .getAppointmentDate()
                            .format(formatter)
            );
            context.setVariable("trackingToken", notification.getTrackingToken());
            context.setVariable("confirmUrl", "http://localhost:8080/webhook/process-confirmation");
            context.setVariable("cancelUrl", "http://localhost:8080/webhook/process-cancellation");
            String html = templateEngine.process("email/email-template", context);


            helper.setText(html, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Erro ao enviar e-mail", e);
        }
    }

    public void sendCancellationConfirmationEmail(Notification notification, String reason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(notification.getPatient().getEmail());
            helper.setSubject("Cancelamento confirmado - FilaZero");
            helper.setFrom("filazerofiap@gmail.com");

            Context context = new Context();
            context.setVariable("patientName", notification.getPatient().getName());
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
            context.setVariable("appointmentDate", 
                notification.getAppointment().getAppointmentDate().format(formatter));
            context.setVariable("doctorName", notification.getAppointment().getDoctor().getName());
            context.setVariable("reason", reason != null ? reason : "Não informado");
            
            String html = templateEngine.process("email/cancellation-confirmation", context);
            helper.setText(html, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Erro ao enviar e-mail de confirmação de cancelamento", e);
        }
    }

}
