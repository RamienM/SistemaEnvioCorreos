package org.secr.sistemaenviocorreos.service;

import jakarta.mail.MessagingException;
import org.secr.sistemaenviocorreos.dto.EmailDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;

@Service
public class EmailConsumer {

    private static final Logger logger = Logger.getLogger(EmailConsumer.class.getName());
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.sender}")
    private String sender;

    @Value("${spring.retry.send.delay}")
    private Integer delay;

    @Value("${spring.retry.send.tries}")
    private Integer tries;

    /**
     * Lectura de correos de una cola de RabbitMQ. Estos correos se envian por un SMTP, en caso de que haya cualquier error
     * se reintentará el envio haciendo uso de un Scheduler.
     * @param emailDTO Correo que se desea mandar
     */
    @RabbitListener(queues = "${rabbitmq.queue}")
    public void sendEmail(EmailDTO emailDTO) {
        JavaMailSenderImpl mailSenderImpl = (JavaMailSenderImpl) mailSender;
        try {
            mailSenderImpl.testConnection();
            logger.info("Conexión SMTP exitosa.");

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(sender);
            message.setTo(emailDTO.email());
            message.setSubject(emailDTO.subject());
            message.setText(emailDTO.message());

            logger.info("Enviando correo a: " + emailDTO.email());
            mailSender.send(message);
            logger.info("Correo enviado exitosamente a: " + emailDTO.email());

        } catch (MessagingException e) {
            logger.log(Level.WARNING, "No se pudo conectar al servidor SMTP: ", e);
            tries -= 1;
            retryLater(emailDTO);
        } catch (MailAuthenticationException e) {
            logger.log(Level.SEVERE, "Error de autenticación al enviar el correo: ", e);
        } catch (MailSendException e) {
            logger.log(Level.SEVERE, "Error al enviar el correo: ", e);
        }
    }

    //Función que permite el reenvio de correos cada cierto tiempo
    private void retryLater(EmailDTO emailDTO) {
        if (tries <= 0) {
            logger.warning("Se agotaron los reintentos para: " + emailDTO.email());
            return;
        }

        Runnable retryTask = () -> {
            logger.info("Reintentando envío de correo a: " + emailDTO.email() + ". Intentos restantes: " + tries);
            sendEmail(emailDTO);};

        scheduler.schedule(retryTask, delay, TimeUnit.SECONDS);
    }
}
