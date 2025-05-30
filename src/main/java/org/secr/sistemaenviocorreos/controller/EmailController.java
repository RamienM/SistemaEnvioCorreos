package org.secr.sistemaenviocorreos.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.secr.sistemaenviocorreos.dto.EmailDTO;
import org.secr.sistemaenviocorreos.dto.ScheduledEmailDTO;
import org.secr.sistemaenviocorreos.service.EmailPublisher;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
@AllArgsConstructor
@RequestMapping("/email")
public class EmailController {

    private final EmailPublisher emailPublisher;
    private static final Logger logger = Logger.getLogger(EmailController.class.getName());

    @PostMapping("/send")
    public ResponseEntity<Void> send(@Valid  @RequestBody EmailDTO emailDTO) {
        try {
            emailPublisher.send(emailDTO);
        }catch (AmqpException e){
            logger.warning("Error al encolar el correo.");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/sendLater")
    public ResponseEntity<Void> sendLater(@Valid @RequestBody ScheduledEmailDTO scheduledEmailDTO) {
        try {
            emailPublisher.sendLater(scheduledEmailDTO);
        }catch (AmqpException e){
            logger.warning("Error al encolar el correo.");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok().build();
    }
}
