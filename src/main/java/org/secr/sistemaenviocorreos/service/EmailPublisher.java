package org.secr.sistemaenviocorreos.service;

import org.secr.sistemaenviocorreos.dto.EmailDTO;
import org.secr.sistemaenviocorreos.dto.PublishRabbitMQDTO;
import org.secr.sistemaenviocorreos.dto.ScheduledEmailDTO;
import org.secr.sistemaenviocorreos.service.interfaces.PublisherInterface;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class EmailPublisher implements PublisherInterface {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final Logger logger = Logger.getLogger(EmailPublisher.class.getName());

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;

    /**
     * Publicación de un mensaje a una cola RabbitMQ. El mensaje se guarda de manera persistente para que en caso de caida
     * se recupere.
     * @param publishRabbitMQDTO          Objeto de transferencia
     * @throws AmqpException    Excepción lanzada cuando hay un problema con el encolamiento de un mensaje
     */
    @Override
    public void publish(PublishRabbitMQDTO publishRabbitMQDTO) throws AmqpException {
        logger.info("Encolando correo...");
        rabbitTemplate.convertAndSend(exchange, routingKey, publishRabbitMQDTO, message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        });
        logger.info("Correo " + publishRabbitMQDTO.email() + " encolado correctamente");
    }


    public void send(EmailDTO emailDTO) {
        PublishRabbitMQDTO message = new PublishRabbitMQDTO(emailDTO.email(),
                emailDTO.subject(),
                emailDTO.message(),
                null,
                3);
        publish(message);
    }

    /**
     * Publicación de envío de mensajes programados. Se controla el caso en el que el mensaje se tenga que enviar
     * direcamente.
     * @param scheduledEmailDTO Objeto de transferencia de Correo y Tiempo programado
     */
    public void sendLater(ScheduledEmailDTO scheduledEmailDTO) {
        PublishRabbitMQDTO message = new PublishRabbitMQDTO(scheduledEmailDTO.email().email(),
                scheduledEmailDTO.email().subject(),
                scheduledEmailDTO.email().message(),
                scheduledEmailDTO.scheduled(),
                3);
        publish(message);
    }


}
