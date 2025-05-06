package org.secr.sistemaenviocorreos.service.interfaces;

import org.secr.sistemaenviocorreos.dto.EmailDTO;
import org.secr.sistemaenviocorreos.dto.ScheduledEmailDTO;

public interface PublisherInterface {
    void publish(EmailDTO emailDTO);
    void publishLater(ScheduledEmailDTO scheduledEmailDTO);
}
