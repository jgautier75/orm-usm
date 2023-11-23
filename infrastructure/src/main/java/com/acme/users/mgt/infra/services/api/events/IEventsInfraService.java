package com.acme.users.mgt.infra.services.api.events;

import com.acme.jga.users.mgt.domain.events.v1.AuditEvent;
import com.acme.jga.users.mgt.exceptions.TechnicalException;

public interface IEventsInfraService {
    String createEvent(AuditEvent auditEvent) throws TechnicalException;
}
