package org.secr.sistemaenviocorreos.service;

import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.eclipse.angus.mail.util.MailConnectException;
import org.secr.sistemaenviocorreos.dto.EmailDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;

@Service
@AllArgsConstructor
public class EmailConsumer {

    private static final Logger logger = Logger.getLogger(EmailConsumer.class.getName());
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final JavaMailSender mailSender;
    private JavaMailSenderImpl mailSenderImpl;

    @RabbitListener(queues = "${rabbitmq.queue}")
    public void sendEmail(EmailDTO emailDTO) {
        sendEmail(emailDTO, 3); // Empieza con 3 intentos
    }

    //Los intetos permite evitar los reintentos infinitos
    public void sendEmail(EmailDTO emailDTO, int retriesLeft) {
        mailSenderImpl = (JavaMailSenderImpl) mailSender;
        try {
            mailSenderImpl.testConnection();
            logger.info("Conexión SMTP exitosa.");

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("rubenduque12345@gmail.com");
            message.setTo(emailDTO.email());
            message.setSubject(emailDTO.subject());
            message.setText(emailDTO.message());

            logger.info("Enviando correo a: " + emailDTO.email());
            mailSender.send(message);
            logger.info("Correo enviado exitosamente a: " + emailDTO.email());

        } catch (MessagingException e) {
            logger.log(Level.WARNING, "No se pudo conectar al servidor SMTP: ", e);
            retryLater(emailDTO, retriesLeft - 1);
        } catch (MailAuthenticationException e) {
            logger.log(Level.SEVERE, "Error de autenticación al enviar el correo: ", e);
        } catch (MailSendException e) {
            logger.log(Level.SEVERE, "Error al enviar el correo: ", e);
        }
    }

    private void retryLater(EmailDTO emailDTO, int retriesLeft) {
        if (retriesLeft <= 0) {
            logger.warning("Se agotaron los reintentos para: " + emailDTO.email());
            return;
        }

        Runnable retryTask = () -> {
            logger.info("Reintentando envío de correo a: " + emailDTO.email() + ". Intentos restantes: " + retriesLeft);
            sendEmail(emailDTO, retriesLeft);
        };

        int delaySeconds = 30;
        scheduler.schedule(retryTask, delaySeconds, TimeUnit.SECONDS);
    }
}
