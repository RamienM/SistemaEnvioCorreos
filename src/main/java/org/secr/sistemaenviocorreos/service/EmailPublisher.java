package org.secr.sistemaenviocorreos.service;

import org.secr.sistemaenviocorreos.dto.EmailDTO;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class EmailPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private CachingConnectionFactory connectionFactory;

    private static final Logger logger = Logger.getLogger(EmailPublisher.class.getName());

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;

    public void publish(EmailDTO emailDTO) throws AmqpException {
        try {
            logger.info("Encolando correo...");
            rabbitTemplate.convertAndSend(exchange, routingKey, emailDTO, message -> {
                message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return message;
            });
            logger.info("Correo " + emailDTO.email() + " encolado correctamente");
        }catch (AmqpException e) {
            logger.warning("Error al encolar el correo.");
        }
    }

}
