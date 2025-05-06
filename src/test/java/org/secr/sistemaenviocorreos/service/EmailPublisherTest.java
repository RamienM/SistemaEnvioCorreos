package org.secr.sistemaenviocorreos.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secr.sistemaenviocorreos.dto.EmailDTO;
import org.secr.sistemaenviocorreos.dto.PublishRabbitMQDTO;
import org.secr.sistemaenviocorreos.dto.ScheduledEmailDTO;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

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
        LocalDateTime date = LocalDateTime.now();
        Integer retry = 1;

        PublishRabbitMQDTO payload = new PublishRabbitMQDTO(email, subject, body, date, retry);

        String exchange = "mi-exchange";
        String routingKey = "mi-routing-key";


        ReflectionTestUtils.setField(emailPublisher, "exchange", exchange);
        ReflectionTestUtils.setField(emailPublisher, "routingKey", routingKey);

        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(PublishRabbitMQDTO.class), any(MessagePostProcessor.class));

        // Act
        emailPublisher.publish(payload);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(PublishRabbitMQDTO.class), any(MessagePostProcessor.class));
    }

    @Test
    void sendEmailTest() {
        // Arrange
        String email = "test@test.com";
        String subject = "Test";
        String body = "Cuerpo de prueba";

        EmailDTO emailDTO = new EmailDTO(email, subject, body);

        String exchange = "mi-exchange";
        String routingKey = "mi-routing-key";


        ReflectionTestUtils.setField(emailPublisher, "exchange", exchange);
        ReflectionTestUtils.setField(emailPublisher, "routingKey", routingKey);

        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(PublishRabbitMQDTO.class), any(MessagePostProcessor.class));

        // Act
        emailPublisher.send(emailDTO);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(PublishRabbitMQDTO.class), any(MessagePostProcessor.class));
    }

    @Test
    void sendEmailLaterTest() {
        // Arrange
        String email = "test@test.com";
        String subject = "Test";
        String body = "Cuerpo de prueba";
        LocalDateTime date = LocalDateTime.now();
        EmailDTO emailDTO = new EmailDTO(email,subject,body);
        ScheduledEmailDTO scheduledEmailDTO = new ScheduledEmailDTO(emailDTO,date);

        String exchange = "mi-exchange";
        String routingKey = "mi-routing-key";


        ReflectionTestUtils.setField(emailPublisher, "exchange", exchange);
        ReflectionTestUtils.setField(emailPublisher, "routingKey", routingKey);

        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(PublishRabbitMQDTO.class), any(MessagePostProcessor.class));

        // Act
        emailPublisher.sendLater(scheduledEmailDTO);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(PublishRabbitMQDTO.class), any(MessagePostProcessor.class));
    }

}
