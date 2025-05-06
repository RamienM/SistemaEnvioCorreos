package org.secr.sistemaenviocorreos.enums;

import lombok.Getter;
import org.secr.sistemaenviocorreos.dto.PublishRabbitMQDTO;

@Getter
public enum EmailTemplateType {
    WELCOME("welcome-email", PublishRabbitMQDTO.class); //Escalable

    private final String templateName;
    private final Class<?> dtoClass;

    //Constructor
    EmailTemplateType(String templateName, Class<?> dtoClass) {
        this.templateName = templateName;
        this.dtoClass = dtoClass;
    }

}
