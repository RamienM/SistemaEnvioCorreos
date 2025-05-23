package org.secr.sistemaenviocorreos.service;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.eclipse.angus.mail.util.MailConnectException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secr.sistemaenviocorreos.dto.PublishRabbitMQDTO;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailConsumerTest {
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private JavaMailSenderImpl mailSenderImpl;
    @Mock
    private EmailTemplateRenderer emailTemplateRenderer;
    @Mock
    private MimeMessage mimeMessage;
    @InjectMocks
    private EmailConsumer emailConsumer;

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

        String renderedMessage = "<html>Email Body</html>";

        when(emailTemplateRenderer.render(any(), eq(payload))).thenReturn(renderedMessage);
        when(mailSenderImpl.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSenderImpl).send(any(MimeMessage.class));

        //Act
        emailConsumer.consumer(payload);

        //Assert
        verify(mailSenderImpl, times(1)).send(any(MimeMessage.class));
        verify(mailSenderImpl, times(1)).createMimeMessage();
        verify(emailTemplateRenderer, times(1)).render(any(), eq(payload));
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

        String renderedMessage = "<html>Email Body</html>";

        when(emailTemplateRenderer.render(any(), eq(payload))).thenReturn(renderedMessage);
        when(mailSenderImpl.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSenderImpl).send(any(MimeMessage.class));

        //Act
        emailConsumer.consumer(payload);

        //Assert
        verify(mailSenderImpl, times(1)).send(any(MimeMessage.class));
        verify(mailSenderImpl, times(1)).createMimeMessage();
        verify(emailTemplateRenderer, times(1)).render(any(), eq(payload));
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

        String renderedMessage = "<html>Email Body</html>";



        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        ScheduledExecutorService schedulerMock = mock(ScheduledExecutorService.class);
        ReflectionTestUtils.setField(emailConsumer, "scheduler", schedulerMock); // solo si no puedes usar constructor

        ScheduledFuture<?> futureMock = mock(ScheduledFuture.class);

        when(schedulerMock.schedule(captor.capture(), anyLong(), any()))
                .thenReturn((ScheduledFuture) futureMock);
        when(emailTemplateRenderer.render(any(), eq(payload))).thenReturn(renderedMessage);
        when(mailSenderImpl.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSenderImpl).send(any(MimeMessage.class));

        //Act
        emailConsumer.consumer(payload);
        captor.getValue().run();

        //Assert
        verify(mailSenderImpl, times(1)).send(any(MimeMessage.class));
        verify(mailSenderImpl, times(1)).createMimeMessage();
        verify(emailTemplateRenderer, times(1)).render(any(), eq(payload));
        verify(schedulerMock, times(1)).schedule(captor.capture(), anyLong(), any());
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

        String renderedMessage = "<html>Email Body</html>";

        when(emailTemplateRenderer.render(any(), eq(payload))).thenReturn(renderedMessage);
        when(mailSenderImpl.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(MailAuthenticationException.class).when(mailSenderImpl).send(any(MimeMessage.class));

        //Act
        emailConsumer.consumer(payload);

        //Assert
        verify(mailSenderImpl, times(1)).send(any(MimeMessage.class));
        verify(mailSenderImpl, times(1)).createMimeMessage();
        verify(emailTemplateRenderer, times(1)).render(any(), eq(payload));
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

        String renderedMessage = "<html>Email Body</html>";

        when(emailTemplateRenderer.render(any(), eq(payload))).thenReturn(renderedMessage);
        when(mailSenderImpl.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(MailSendException.class).when(mailSenderImpl).send(any(MimeMessage.class));

        //Act
        emailConsumer.consumer(payload);

        //Assert
        verify(mailSenderImpl, times(1)).send(any(MimeMessage.class));
        verify(mailSenderImpl, times(1)).createMimeMessage();
        verify(emailTemplateRenderer, times(1)).render(any(), eq(payload));
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
    void sendEmailWhenSMTPConnectionFailTest() throws MessagingException {
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

        doThrow(MailConnectException.class).when(mailSenderImpl).testConnection();

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
    void sendEmailWhenOtherExceptionTest() throws MessagingException {
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

        doThrow(RuntimeException.class).when(mailSenderImpl).testConnection();

        //Act
        emailConsumer.consumer(payload);

        //Assert
        verify(mailSenderImpl, times(1)).testConnection();
    }

    @Test
    void sendEmailWhenRetryIs0Test() throws MessagingException {
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
    void rePublishInCaseOfExceptionTest() throws MessagingException {
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
