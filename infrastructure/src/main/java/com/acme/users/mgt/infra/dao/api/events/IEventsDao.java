package com.acme.users.mgt.infra.dao.api.events;

import java.sql.SQLException;

import com.acme.users.mgt.infra.dto.events.v1.AuditEventDb;

public interface IEventsDao {

    String insertEvent(AuditEventDb event) throws SQLException;

    AuditEventDb findByUid(String uid);

}
