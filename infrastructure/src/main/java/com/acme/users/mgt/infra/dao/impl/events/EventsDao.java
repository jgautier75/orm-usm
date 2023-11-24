package com.acme.users.mgt.infra.dao.impl.events;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import com.acme.jga.users.mgt.dao.jdbc.spring.AbstractJdbcDaoSupport;
import com.acme.jga.users.mgt.dao.jdbc.utils.DaoConstants;
import com.acme.jga.users.mgt.domain.events.v1.AuditEvent;
import com.acme.users.mgt.infra.dao.api.events.IEventsDao;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class EventsDao extends AbstractJdbcDaoSupport implements IEventsDao {
    private ObjectMapper mapper;

    public EventsDao(DataSource ds, NamedParameterJdbcTemplate namedParameterJdbcTemplate, ObjectMapper objectMapper) {
        super(ds, namedParameterJdbcTemplate);
        super.loadQueryFilePath(new String[] { "events.properties" });
        this.mapper = objectMapper;
    }

    @Override
    public String insertEvent(AuditEvent event) throws JsonProcessingException, SQLException {
        String baseQuery = super.getQuery("event_create");
        String uuid = DaoConstants.generatedUUID();
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_UID, uuid);
        params.put("pCreatedAt", event.getTimestamp());
        params.put("pUpdatedAt", event.getTimestamp());
        params.put("pTarget", event.getTarget().getValue());
        params.put("pObjectUid", event.getObjectUid());
        params.put("pAction", event.getAction().name());
        params.put("pStatus", event.getStatus().getValue());
        String jsonedEvent = this.mapper.writeValueAsString(event);
        params.put("pPayload", super.buildPGobject(jsonedEvent));
        super.getNamedParameterJdbcTemplate().update(baseQuery, params);
        return uuid;
    }

}
