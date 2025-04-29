package org.secr.sistemaenviocorreos.api_rest;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.secr.sistemaenviocorreos.dto.EmailDTO;
import org.secr.sistemaenviocorreos.service.EmailPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/email")
public class EmailAPI {

    private final EmailPublisher emailPublisher;

    @PostMapping("/send")
    public ResponseEntity<Void> send(@Valid  @RequestBody EmailDTO emailDTO) {
        emailPublisher.publish(emailDTO);
        return ResponseEntity.ok().build();
    }
}
