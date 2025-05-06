package org.secr.sistemaenviocorreos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ScheduledEmailDTO (@NotNull @Valid EmailDTO email, @NotNull LocalDateTime scheduled){}
