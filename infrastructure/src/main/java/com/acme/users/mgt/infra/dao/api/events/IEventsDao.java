package com.acme.users.mgt.infra.dao.api.events;

import java.sql.SQLException;
import java.util.List;

import com.acme.jga.users.mgt.domain.events.v1.EventStatus;
import com.acme.users.mgt.infra.dto.events.v1.AuditEventDb;

public interface IEventsDao {

    String insertEvent(AuditEventDb event) throws SQLException;

    AuditEventDb findByUid(String uid);

    List<AuditEventDb> findPendingEvents();

    Integer updateEvents(List<String> eventsUidList, EventStatus eventStatus);

}
