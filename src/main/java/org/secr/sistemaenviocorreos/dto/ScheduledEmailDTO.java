package org.secr.sistemaenviocorreos.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ScheduledEmailDTO (@NotNull EmailDTO email, @NotNull LocalDateTime scheduled){}
