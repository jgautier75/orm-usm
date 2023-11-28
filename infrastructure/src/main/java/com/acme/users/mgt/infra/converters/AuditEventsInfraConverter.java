package com.acme.users.mgt.infra.converters;

import org.springframework.stereotype.Component;

import com.acme.jga.users.mgt.domain.events.v1.AuditEvent;
import com.acme.users.mgt.infra.dto.events.v1.AuditEventDb;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AuditEventsInfraConverter {

    public AuditEventDb convertAuditEventToDb(AuditEvent auditEvent, ObjectMapper objectMapper)
            throws JsonProcessingException {
        AuditEventDb auditEventDb = null;
        if (auditEvent != null) {
            String payload = objectMapper.writeValueAsString(auditEvent);
            auditEventDb = AuditEventDb.builder()
                    .action(auditEvent.getAction())
                    .createdAt(auditEvent.getCreatedAt())
                    .lastUpdatedAt(auditEvent.getLastUpdatedAt())
                    .objectUid(auditEvent.getObjectUid())
                    .payload(payload)
                    .status(auditEvent.getStatus())
                    .target(auditEvent.getTarget())
                    .uid(auditEvent.getUid())
                    .build();
        }
        return auditEventDb;
    }

}
