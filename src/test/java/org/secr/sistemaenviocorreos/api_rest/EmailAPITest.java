package org.secr.sistemaenviocorreos.api_rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secr.sistemaenviocorreos.dto.EmailDTO;
import org.secr.sistemaenviocorreos.service.EmailPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = EmailAPI.class)
public class EmailAPITest {

    @MockitoBean
    private EmailPublisher emailPublisher;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void sendEmailTest() throws Exception {
        //Arrange
        String email = "test@test.com";
        String subject = "Test";
        String body = "Test";
        EmailDTO emailDTO = new EmailDTO(email, subject, body);

        doNothing().when(emailPublisher).publish(any(EmailDTO.class));

        //Act
        ResultActions resp = mockMvc.perform(post("/email/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailDTO)));

        //Assert
        resp.andExpect(status().isOk());
        verify(emailPublisher,times(1)).publish(any(EmailDTO.class));
    }

    @Test
    void sendEmailWhenEmailIsWrongException() throws Exception {
        //Arrange
        String email = "testtest.com";
        String subject = "Test";
        String body = "Test";
        EmailDTO emailDTO = new EmailDTO(email, subject, body);

        //Act
        ResultActions resp = mockMvc.perform(post("/email/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailDTO)));

        //Assert
        resp.andExpect(status().is(400));
    }
    @Test
    void sendEmailWhenEmailIsBlanckException() throws Exception {
        //Arrange
        String email = "";
        String subject = "Test";
        String body = "Test";
        EmailDTO emailDTO = new EmailDTO(email, subject, body);

        //Act
        ResultActions resp = mockMvc.perform(post("/email/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailDTO)));

        //Assert
        resp.andExpect(status().is(400));
    }
    @Test
    void sendEmailWhenSubjectlIsBlanckException() throws Exception {
        //Arrange
        String email = "test@test.com";
        String subject = "";
        String body = "Test";
        EmailDTO emailDTO = new EmailDTO(email, subject, body);

        //Act
        ResultActions resp = mockMvc.perform(post("/email/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailDTO)));

        //Assert
        resp.andExpect(status().is(400));
    }

    @Test
    void sendEmailWhenBodylIsBlanckException() throws Exception {
        //Arrange
        String email = "test@test.com";
        String subject = "Test";
        String body = "";
        EmailDTO emailDTO = new EmailDTO(email, subject, body);

        //Act
        ResultActions resp = mockMvc.perform(post("/email/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailDTO)));

        //Assert
        resp.andExpect(status().is(400));
    }
}
