package com.acme.users.mgt.infra.dao.extractors;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.acme.jga.users.mgt.dao.jdbc.utils.SQLExtractor;
import com.acme.jga.users.mgt.domain.events.v1.AuditAction;
import com.acme.jga.users.mgt.domain.events.v1.EventStatus;
import com.acme.jga.users.mgt.domain.events.v1.EventTarget;
import com.acme.users.mgt.infra.dto.events.v1.AuditEventDb;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuditEventDbExtractor {
    public static AuditEventDb extractAuditEvent(ResultSet resultSet, boolean checkNext) throws SQLException {
        AuditEventDb evt = null;
        if (!checkNext || resultSet.next()) {
            evt = AuditEventDb.builder()
                    .action(AuditAction.valueOf(SQLExtractor.extractString(resultSet, "action")))
                    .createdAt(SQLExtractor.extractLocalDateTime(resultSet, "created_at"))
                    .lastUpdatedAt(SQLExtractor.extractLocalDateTime(resultSet, "last_updated_at"))
                    .objectUid(SQLExtractor.extractString(resultSet, "object_uid"))
                    .payload(SQLExtractor.extractString(resultSet, "payload"))
                    .status(EventStatus.fromValue(SQLExtractor.extractInteger(resultSet, "status")))
                    .target(EventTarget.fromValue(SQLExtractor.extractInteger(resultSet, "target")))
                    .uid(SQLExtractor.extractString(resultSet, "uid")).build();
        }
        return evt;
    }
}
