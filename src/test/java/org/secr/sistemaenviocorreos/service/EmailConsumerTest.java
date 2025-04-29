package org.secr.sistemaenviocorreos.service;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secr.sistemaenviocorreos.dto.EmailDTO;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailConsumerTest {
    @Mock
    private JavaMailSenderImpl mailSenderImpl;
    @Mock
    private ScheduledExecutorService scheduledExecutorService;
    @InjectMocks
    private EmailConsumer emailConsumer;


    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor; //RECOMENDADO POR GPT

    @Test
    void sendEmailBasicTest(){
        //Arrange
        String email = "test@test.com";
        String subject = "Test";
        String body = "Cuerpo de prueba";
        EmailDTO emailDTO = new EmailDTO(email, subject, body);

        doNothing().when(mailSenderImpl).send(any(SimpleMailMessage.class));

        //Act
        emailConsumer.sendEmail(emailDTO);

        //Assert
        verify(mailSenderImpl, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sent = messageCaptor.getValue();
        assertEquals("test@test.com", sent.getTo()[0]);
        assertEquals("Test", sent.getSubject());
        assertEquals("Cuerpo de prueba", sent.getText());
    }

    @Test
    void sendEmailTest(){
        //Arrange
        String email = "test@test.com";
        String subject = "Test";
        String body = "Cuerpo de prueba";
        EmailDTO emailDTO = new EmailDTO(email, subject, body);

        doNothing().when(mailSenderImpl).send(any(SimpleMailMessage.class));

        //Act
        emailConsumer.sendEmail(emailDTO);

        //Assert
        verify(mailSenderImpl, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sent = messageCaptor.getValue();
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
        EmailDTO emailDTO = new EmailDTO(email, subject, body);

        doThrow(MailAuthenticationException.class).when(mailSenderImpl).send(any(SimpleMailMessage.class));

        //Act
        emailConsumer.sendEmail(emailDTO);

        //Assert
        verify(mailSenderImpl, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmailWhenMailSendExceptionTest(){
        //Arrange
        String email = "test@test.com";
        String subject = "Test";
        String body = "Cuerpo de prueba";
        EmailDTO emailDTO = new EmailDTO(email, subject, body);

        doThrow(MailSendException.class).when(mailSenderImpl).send(any(SimpleMailMessage.class));

        //Act
        emailConsumer.sendEmail(emailDTO);

        //Assert
        verify(mailSenderImpl, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmailWhenConnectionFailTest() throws MessagingException {
        //Arrange
        String email = "test@test.com";
        String subject = "Test";
        String body = "Cuerpo de prueba";
        EmailDTO emailDTO = new EmailDTO(email, subject, body);

        doThrow(MessagingException.class).when(mailSenderImpl).testConnection();

        //Act
        emailConsumer.sendEmail(emailDTO);

        //Assert
        verify(mailSenderImpl, times(1)).testConnection();
    }

    @Test
    void retryLaterTest() throws MessagingException { //No se como comprobarlo
        // Arrange
        String email = "test@test.com";
        String subject = "Test";
        String body = "Cuerpo de prueba";
        EmailDTO emailDTO = new EmailDTO(email, subject, body);

        doThrow(MessagingException.class).when(mailSenderImpl).testConnection();
        when(scheduledExecutorService.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class))).thenReturn(null);
        // Act
        emailConsumer.sendEmail(emailDTO);

        // Assert:
        verify(mailSenderImpl, times(1)).testConnection();
        verify(scheduledExecutorService, times(1)).schedule(any(Runnable.class), any(Integer.class), any(TimeUnit.class));
    }
}
