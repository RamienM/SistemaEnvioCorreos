package org.secr.sistemaenviocorreos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailDTO(
        @NotBlank(message = "Email is required") @Email(message = "Email is wrong")
        String email,
        @NotBlank(message = "Subject is required")
        String subject,
        @NotBlank(message = "Message is required")
        String message

) {}
