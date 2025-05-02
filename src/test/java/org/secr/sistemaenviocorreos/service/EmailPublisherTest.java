package org.secr.sistemaenviocorreos.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secr.sistemaenviocorreos.dto.EmailDTO;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailPublisherTest {
    @Mock
    private RabbitTemplate rabbitTemplate;
    @InjectMocks
    private EmailPublisher emailPublisher;

    @Test
    void publishTest() {
        // Arrange
        String email = "test@test.com";
        String subject = "Test";
        String body = "Cuerpo de prueba";

        EmailDTO emailDTO = new EmailDTO(email, subject, body);

        String exchange = "mi-exchange";
        String routingKey = "mi-routing-key";


        ReflectionTestUtils.setField(emailPublisher, "exchange", exchange);
        ReflectionTestUtils.setField(emailPublisher, "routingKey", routingKey);

        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(EmailDTO.class), any(MessagePostProcessor.class));

        // Act
        emailPublisher.publish(emailDTO);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(EmailDTO.class), any(MessagePostProcessor.class));
    }

}
