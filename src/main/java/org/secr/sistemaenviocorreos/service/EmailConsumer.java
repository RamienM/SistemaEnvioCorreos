package org.secr.sistemaenviocorreos.service;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import org.eclipse.angus.mail.util.MailConnectException;
import org.secr.sistemaenviocorreos.dto.PublishRabbitMQDTO;
import org.secr.sistemaenviocorreos.service.interfaces.ConsumerInterface;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;

@Service
public class EmailConsumer implements ConsumerInterface {

    private static final Logger logger = Logger.getLogger(EmailConsumer.class.getName());
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;

    @Value("${spring.mail.sender}")
    private String sender;

    @Value("${spring.retry.send.delay}")
    private Integer delay;

    /**
     * Lectura de correos de una cola de RabbitMQ. Estos correos se envian por un SMTP, en caso de que haya cualquier error
     * se reintentará el envio haciendo uso de un Scheduler.
     * @param rMQMessage Correo que se desea mandar
     */
    @Override
    @RabbitListener(queues = "${rabbitmq.queue}")
    public void consumer(PublishRabbitMQDTO rMQMessage) {
        if (rMQMessage.retry() <= 0){
            logger.warning("Se agotaron los reintentos para: " + rMQMessage.email());
            return;
        }
        if (rMQMessage.sendDate() == null)sendEmail(rMQMessage);
        else {

            LocalDateTime scheduledTime = rMQMessage.sendDate();
            long delayMillis = Duration.between(LocalDateTime.now(), scheduledTime).toMillis();

            if (delayMillis <= 0) sendEmail(rMQMessage);
            else {
                logger.info("Correo programado para: " + rMQMessage.email() + " a las " + scheduledTime);

                scheduler.schedule(() -> {
                    logger.info("Ejecutando envío programado a: " + rMQMessage.email());
                    sendEmail(rMQMessage);
                }, delayMillis, TimeUnit.MILLISECONDS);
            }
        }
    }

    private void sendEmail(PublishRabbitMQDTO rMQMessage) {
        JavaMailSenderImpl mailSenderImpl = (JavaMailSenderImpl) mailSender;
        try {
            mailSenderImpl.testConnection(); //Prueba de conexion con el servidor SMTP
            logger.info("Conexión SMTP exitosa.");

            //Preparamos el mail
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(sender);
            message.setTo(rMQMessage.email());
            message.setSubject(rMQMessage.subject());
            message.setText(rMQMessage.message());

            logger.info("Enviando correo a: " + rMQMessage.email());
            mailSender.send(message);
            logger.info("Correo enviado exitosamente a: " + rMQMessage.email());

        }catch (MailConnectException e){
            logger.log(Level.SEVERE, "No se pudo conectar con el host SMTP: ", e);
            rePublishInCaseOfException(rMQMessage);
        }catch (AuthenticationFailedException e){
            logger.log(Level.SEVERE, "Autenticación fallida: ", e);
            rePublishInCaseOfException(rMQMessage);
        } catch (MessagingException e) {
            logger.log(Level.WARNING, "No se pudo conectar al servidor SMTP: ", e);
            rePublishInCaseOfException(rMQMessage);
        } catch (MailAuthenticationException e) {
            logger.log(Level.SEVERE, "Error de autenticación al enviar el correo: ", e);
            rePublishInCaseOfException(rMQMessage);
        } catch (MailSendException e) {
            logger.log(Level.SEVERE, "Error al enviar el correo: ", e);
            rePublishInCaseOfException(rMQMessage);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ocurrió un error inesperado al enviar el correo: ", e);
            rePublishInCaseOfException(rMQMessage);
        }
    }

    private void rePublishInCaseOfException(PublishRabbitMQDTO rMQMessage) {
        logger.info("Rencolando correo...");
        PublishRabbitMQDTO publishRabbitMQDTO = new PublishRabbitMQDTO(rMQMessage.email(),
                rMQMessage.subject(),
                rMQMessage.message(),
                rMQMessage.sendDate(),
                rMQMessage.retry()-1);

        rabbitTemplate.convertAndSend(exchange, routingKey, publishRabbitMQDTO, message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            message.getMessageProperties().setHeader("x-delay", delay*1000); //seconds
            return message;
        });


        logger.info("Correo " + rMQMessage.email() + " rencolado correctamente. Intentos restantes: " + rMQMessage.retry());
    }
}
