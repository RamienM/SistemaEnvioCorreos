package org.secr.sistemaenviocorreos.service;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secr.sistemaenviocorreos.dto.PublishRabbitMQDTO;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailConsumerTest {
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private JavaMailSenderImpl mailSenderImpl;
    @InjectMocks
    private EmailConsumer emailConsumer;


    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor; //RECOMENDADO POR GPT

    @Test
    void sendEmailNowTest(){
        //Arrange
        String email = "test@test.com";
        String subject = "Test";
        String body = "Cuerpo de prueba";
        LocalDateTime date = LocalDateTime.now();
        Integer retry = 3;

        PublishRabbitMQDTO payload = new PublishRabbitMQDTO(email, subject, body, date, retry);


        String sender = "test@test.com";
        Integer delay = 30;
        ReflectionTestUtils.setField(emailConsumer, "sender", sender);
        ReflectionTestUtils.setField(emailConsumer, "delay", delay);

        doNothing().when(mailSenderImpl).send(any(SimpleMailMessage.class));

        //Act
        emailConsumer.consumer(payload);

        //Assert
        verify(mailSenderImpl, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sent = messageCaptor.getValue();
        assertNotNull(sent.getTo());
        assertEquals("test@test.com", sent.getTo()[0]);
        assertEquals("Test", sent.getSubject());
        assertEquals("Cuerpo de prueba", sent.getText());
    }

    @Test
    void sendEmailWhenTimeStampIsNullTest(){
        //Arrange
        String email = "test@test.com";
        String subject = "Test";
        String body = "Cuerpo de prueba";
        LocalDateTime date = null;
        Integer retry = 3;

        PublishRabbitMQDTO payload = new PublishRabbitMQDTO(email, subject, body, date, retry);


        String sender = "test@test.com";
        Integer delay = 30;
        ReflectionTestUtils.setField(emailConsumer, "sender", sender);
        ReflectionTestUtils.setField(emailConsumer, "delay", delay);

        doNothing().when(mailSenderImpl).send(any(SimpleMailMessage.class));

        //Act
        emailConsumer.consumer(payload);

        //Assert
        verify(mailSenderImpl, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sent = messageCaptor.getValue();
        assertNotNull(sent.getTo());
        assertEquals("test@test.com", sent.getTo()[0]);
        assertEquals("Test", sent.getSubject());
        assertEquals("Cuerpo de prueba", sent.getText());
    }

    @Test
    void sendEmailLaterTest(){
        //Arrange
        String email = "test@test.com";
        String subject = "Test";
        String body = "Cuerpo de prueba";
        LocalDateTime date = LocalDateTime.now().plusMinutes(2);
        Integer retry = 3;

        PublishRabbitMQDTO payload = new PublishRabbitMQDTO(email, subject, body, date, retry);


        String sender = "test@test.com";
        Integer delay = 30;
        ReflectionTestUtils.setField(emailConsumer, "sender", sender);
        ReflectionTestUtils.setField(emailConsumer, "delay", delay);

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        ScheduledExecutorService schedulerMock = mock(ScheduledExecutorService.class);
        ReflectionTestUtils.setField(emailConsumer, "scheduler", schedulerMock); // solo si no puedes usar constructor

        ScheduledFuture<?> futureMock = mock(ScheduledFuture.class);

        when(schedulerMock.schedule(captor.capture(), anyLong(), any()))
                .thenReturn((ScheduledFuture) futureMock);

        doNothing().when(mailSenderImpl).send(any(SimpleMailMessage.class));
        //doNothing().when(scheduledExecutorService).schedule(captor.capture(), anyLong(), any());

        //Act
        emailConsumer.consumer(payload);
        captor.getValue().run();

        //Assert
        verify(mailSenderImpl, times(1)).send(messageCaptor.capture());
        verify(schedulerMock, times(1)).schedule(captor.capture(), anyLong(), any());
        SimpleMailMessage sent = messageCaptor.getValue();
        assertNotNull(sent.getTo());
        assertEquals("test@test.com", sent.getTo()[0]);
        assertEquals("Test", sent.getSubject());
        assertEquals("Cuerpo de prueba", sent.getText());
    }

    @Test
    void sendEmailWhenMailAuthenticationExceptionTest(){
        //Arrange
        String email = "test@test.com";
        String subject = "Test";
        String body = "Cuerpo de prueba";
        LocalDateTime date = LocalDateTime.now();
        Integer retry = 3;

        PublishRabbitMQDTO payload = new PublishRabbitMQDTO(email, subject, body, date, retry);


        String sender = "test@test.com";
        Integer delay = 30;
        ReflectionTestUtils.setField(emailConsumer, "sender", sender);
        ReflectionTestUtils.setField(emailConsumer, "delay", delay);

        doThrow(MailAuthenticationException.class).when(mailSenderImpl).send(any(SimpleMailMessage.class));

        //Act
        emailConsumer.consumer(payload);

        //Assert
        verify(mailSenderImpl, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmailWhenMailSendExceptionTest(){
        //Arrange
        String email = "test@test.com";
        String subject = "Test";
        String body = "Cuerpo de prueba";
        LocalDateTime date = LocalDateTime.now();
        Integer retry = 3;

        PublishRabbitMQDTO payload = new PublishRabbitMQDTO(email, subject, body, date, retry);


        String sender = "test@test.com";
        Integer delay = 30;
        ReflectionTestUtils.setField(emailConsumer, "sender", sender);
        ReflectionTestUtils.setField(emailConsumer, "delay", delay);

        doThrow(MailSendException.class).when(mailSenderImpl).send(any(SimpleMailMessage.class));

        //Act
        emailConsumer.consumer(payload);

        //Assert
        verify(mailSenderImpl, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmailWhenConnectionFailTest() throws MessagingException {
        //Arrange
        String email = "test@test.com";
        String subject = "Test";
        String body = "Cuerpo de prueba";
        LocalDateTime date = LocalDateTime.now();
        Integer retry = 3;

        PublishRabbitMQDTO payload = new PublishRabbitMQDTO(email, subject, body, date, retry);


        String sender = "test@test.com";
        Integer delay = 30;
        ReflectionTestUtils.setField(emailConsumer, "sender", sender);
        ReflectionTestUtils.setField(emailConsumer, "delay", delay);

        doThrow(MessagingException.class).when(mailSenderImpl).testConnection();

        //Act
        emailConsumer.consumer(payload);

        //Assert
        verify(mailSenderImpl, times(1)).testConnection();
    }

    @Test
    void sendEmailWhenAuthenticationFailedExceptionTest() throws MessagingException {
        //Arrange
        String email = "test@test.com";
        String subject = "Test";
        String body = "Cuerpo de prueba";
        LocalDateTime date = LocalDateTime.now();
        Integer retry = 3;

        PublishRabbitMQDTO payload = new PublishRabbitMQDTO(email, subject, body, date, retry);


        String sender = "test@test.com";
        Integer delay = 30;
        ReflectionTestUtils.setField(emailConsumer, "sender", sender);
        ReflectionTestUtils.setField(emailConsumer, "delay", delay);

        doThrow(AuthenticationFailedException.class).when(mailSenderImpl).testConnection();

        //Act
        emailConsumer.consumer(payload);

        //Assert
        verify(mailSenderImpl, times(1)).testConnection();
    }

    @Test
    void sendEmailWhenRetryIs0Test() throws MessagingException { //No se como comprobarlo
        // Arrange
        String email = "test@test.com";
        String subject = "Test";
        String body = "Cuerpo de prueba";
        LocalDateTime date = LocalDateTime.now();
        Integer retry = 0;

        PublishRabbitMQDTO payload = new PublishRabbitMQDTO(email, subject, body, date, retry);


        String sender = "test@test.com";
        Integer delay = 30;
        ReflectionTestUtils.setField(emailConsumer, "sender", sender);
        ReflectionTestUtils.setField(emailConsumer, "delay", delay);

        // Act
        emailConsumer.consumer(payload);

        // Assert:
        verify(mailSenderImpl, times(0)).testConnection();
    }

    @Test
    void rePublishInCaseOfExceptionTest() throws MessagingException { //No se como comprobarlo
        // Arrange
        String email = "test@test.com";
        String subject = "Test";
        String body = "Cuerpo de prueba";
        LocalDateTime date = LocalDateTime.now();
        Integer retry = 3;

        PublishRabbitMQDTO payload = new PublishRabbitMQDTO(email, subject, body, date, retry);

        String sender = "test@test.com";
        Integer delay = 30;
        String exchange = "mi-exchange";
        String routingKey = "mi-routing-key";
        ReflectionTestUtils.setField(emailConsumer, "sender", sender);
        ReflectionTestUtils.setField(emailConsumer, "delay", delay);
        ReflectionTestUtils.setField(emailConsumer, "exchange", exchange);
        ReflectionTestUtils.setField(emailConsumer, "routingKey", routingKey);

        doThrow(MessagingException.class).when(mailSenderImpl).testConnection();
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(PublishRabbitMQDTO.class), any(MessagePostProcessor.class));

        // Act
        emailConsumer.consumer(payload);

        // Assert:
        verify(mailSenderImpl, times(1)).testConnection();
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(PublishRabbitMQDTO.class), any(MessagePostProcessor.class));
    }

}
