package com.acme.users.mgt.infra.services.impl.events;

import java.sql.SQLException;

import org.springframework.stereotype.Service;

import com.acme.jga.users.mgt.domain.events.v1.AuditEvent;
import com.acme.jga.users.mgt.exceptions.TechnicalException;
import com.acme.users.mgt.infra.converters.AuditEventsInfraConverter;
import com.acme.users.mgt.infra.dao.api.events.IEventsDao;
import com.acme.users.mgt.infra.dto.events.v1.AuditEventDb;
import com.acme.users.mgt.infra.services.api.events.IEventsInfraService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventsInfraService implements IEventsInfraService {
    private final IEventsDao eventsDao;
    private final AuditEventsInfraConverter auditEventsInfraConverter;
    private final ObjectMapper objectMapper;

    @Override
    public String createEvent(AuditEvent auditEvent) throws TechnicalException {
        try {
            AuditEventDb auditEventDb = auditEventsInfraConverter.convertAuditEventToDb(auditEvent, objectMapper);
            return eventsDao.insertEvent(auditEventDb);
        } catch (JsonProcessingException | SQLException e) {
            throw new TechnicalException("Unable to persist event", e);
        }
    }

}
