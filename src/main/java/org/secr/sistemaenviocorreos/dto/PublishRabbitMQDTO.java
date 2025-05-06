package org.secr.sistemaenviocorreos.dto;

import java.time.LocalDateTime;

public record PublishRabbitMQDTO (
        String email,
        String subject,
        String message,
        LocalDateTime sendDate,
        Integer retry
){
}
