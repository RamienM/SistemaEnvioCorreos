package org.secr.sistemaenviocorreos.service.interfaces;

import org.secr.sistemaenviocorreos.dto.EmailDTO;

public interface ConsumerInterface {
    void sendEmail(EmailDTO emailDTO);
}
