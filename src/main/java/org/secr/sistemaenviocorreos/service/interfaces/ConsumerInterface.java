package org.secr.sistemaenviocorreos.service.interfaces;

import org.secr.sistemaenviocorreos.dto.EmailDTO;
import org.secr.sistemaenviocorreos.dto.PublishRabbitMQDTO;

public interface ConsumerInterface {
    void consumer(PublishRabbitMQDTO rMQMessage);
}
