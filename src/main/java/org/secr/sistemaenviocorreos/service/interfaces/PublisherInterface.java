package org.secr.sistemaenviocorreos.service.interfaces;

import org.secr.sistemaenviocorreos.dto.EmailDTO;

public interface PublisherInterface {
    void publish(EmailDTO emailDTO);
}
