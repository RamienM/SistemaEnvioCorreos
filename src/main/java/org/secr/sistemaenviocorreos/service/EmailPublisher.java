package org.secr.sistemaenviocorreos.service;

import org.secr.sistemaenviocorreos.dto.EmailDTO;
import org.secr.sistemaenviocorreos.dto.ScheduledEmailDTO;
import org.secr.sistemaenviocorreos.service.interfaces.PublisherInterface;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class EmailPublisher implements PublisherInterface {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Logger logger = Logger.getLogger(EmailPublisher.class.getName());

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;

    /**
     * Publicación de un mensaje a una cola RabbitMQ. El mensaje se guarda de manera persistente para que en caso de caida
     * se recupere.
     * @param emailDTO          Objeto de transferencia Email
     * @throws AmqpException    Excepción lanzada cuando hay un problema con el encolamiento de un mensaje
     */
    @Override
    public void publish(EmailDTO emailDTO) throws AmqpException {
        logger.info("Encolando correo...");
        rabbitTemplate.convertAndSend(exchange, routingKey, emailDTO, message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        });
        logger.info("Correo " + emailDTO.email() + " encolado correctamente");
    }


    /**
     * Publicación de envío de mensajes programados. Se controla el caso en el que el mensaje se tenga que enviar
     * direcamente.
     * @param scheduledEmailDTO Objeto de transferencia de Correo y Tiempo programado
     */
    @Override
    public void publishLater(ScheduledEmailDTO scheduledEmailDTO) {
        LocalDateTime scheduledTime = scheduledEmailDTO.scheduled();
        long delayMillis = Duration.between(LocalDateTime.now(), scheduledTime).toMillis();

        if (delayMillis <= 0) {
            logger.warning("La fecha/hora programada ya pasó. Enviando inmediatamente a: " + scheduledEmailDTO.email().email());
            publish(scheduledEmailDTO.email());
        } else {
            logger.info("Correo programado para: " + scheduledEmailDTO.email().email() + " a las " + scheduledTime);

            //No me acaba de gustar, en caso de exception se pierde el mensaje?
            scheduler.schedule(() -> {
                logger.info("Ejecutando envío programado a: " + scheduledEmailDTO.email().email());
                publish(scheduledEmailDTO.email());
            }, delayMillis, TimeUnit.MILLISECONDS);
        }
    }
}
