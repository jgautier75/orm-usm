package com.acme.users.mgt.infra.dao.api.events;

import com.acme.jga.users.mgt.domain.events.v1.AuditEvent;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface IEventsDao {

    String insertEvent(AuditEvent event) throws JsonProcessingException;

}
