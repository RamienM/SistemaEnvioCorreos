package org.secr.sistemaenviocorreos.service;

import lombok.AllArgsConstructor;
import org.secr.sistemaenviocorreos.enums.EmailTemplateType;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.TemplateEngine;

import java.util.Map;

@AllArgsConstructor
@Service
public class EmailTemplateRenderer {

    private final TemplateEngine templateEngine;

    public String render(EmailTemplateType templateType, Object data) {
        // Validar tipo del DTO
        if (!templateType.getDtoClass().isInstance(data)) {
            throw new IllegalArgumentException("DTO incorrecto para " + templateType);
        }

        Context context = new Context();
        context.setVariables(Map.of("data", data));
        return templateEngine.process(templateType.getTemplateName(), context);
    }
}
