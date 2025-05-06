package org.secr.sistemaenviocorreos.service.interfaces;

import org.secr.sistemaenviocorreos.dto.PublishRabbitMQDTO;

public interface PublisherInterface {
    void publish(PublishRabbitMQDTO publishRabbitMQDTO);
}
