package org.secr.sistemaenviocorreos.service;

import org.secr.sistemaenviocorreos.dto.EmailDTO;
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
}
