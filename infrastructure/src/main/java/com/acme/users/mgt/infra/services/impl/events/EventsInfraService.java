package com.acme.users.mgt.infra.services.impl.events;

import java.sql.SQLException;

import org.springframework.stereotype.Service;

import com.acme.jga.users.mgt.domain.events.v1.AuditEvent;
import com.acme.jga.users.mgt.exceptions.TechnicalException;
import com.acme.users.mgt.infra.dao.api.events.IEventsDao;
import com.acme.users.mgt.infra.services.api.events.IEventsInfraService;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventsInfraService implements IEventsInfraService {
    private final IEventsDao eventsDao;

    @Override
    public String createEvent(AuditEvent auditEvent) throws TechnicalException {
        try {
            return eventsDao.insertEvent(auditEvent);
        } catch (JsonProcessingException | SQLException e) {
            throw new TechnicalException("Unable to persist event", e);
        }
    }

}
