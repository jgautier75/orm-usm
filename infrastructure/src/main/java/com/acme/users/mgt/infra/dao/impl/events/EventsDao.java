package com.acme.users.mgt.infra.dao.impl.events;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import com.acme.jga.users.mgt.dao.jdbc.spring.AbstractJdbcDaoSupport;
import com.acme.jga.users.mgt.dao.jdbc.utils.DaoConstants;
import com.acme.jga.users.mgt.dto.pagination.WhereClause;
import com.acme.jga.users.mgt.dto.pagination.WhereOperator;
import com.acme.users.mgt.infra.dao.api.events.IEventsDao;
import com.acme.users.mgt.infra.dao.extractors.AuditEventDbExtractor;
import com.acme.users.mgt.infra.dto.events.v1.AuditEventDb;

@Repository
public class EventsDao extends AbstractJdbcDaoSupport implements IEventsDao {
    public EventsDao(DataSource ds, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        super(ds, namedParameterJdbcTemplate);
        super.loadQueryFilePath(new String[] { "events.properties" });
    }

    @Override
    public String insertEvent(AuditEventDb event) throws SQLException {
        String baseQuery = super.getQuery("event_create");
        String uuid = DaoConstants.generatedUUID();
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_UID, uuid);
        params.put("pCreatedAt", event.getCreatedAt());
        params.put("pUpdatedAt", event.getLastUpdatedAt());
        params.put("pTarget", event.getTarget().getValue());
        params.put("pObjectUid", event.getObjectUid());
        params.put("pAction", event.getAction().name());
        params.put("pStatus", event.getStatus().getValue());
        params.put("pPayload", super.buildPGobject(event.getPayload()));
        super.getNamedParameterJdbcTemplate().update(baseQuery, params);
        return uuid;
    }

    @Override
    public AuditEventDb findByUid(String uid) {
        String baseQuery = super.getQuery("event_sel_base");
        List<WhereClause> whereClauses = new ArrayList<>();
        whereClauses.add(WhereClause.builder().expression(DaoConstants.FIELD_UID + "=:" + DaoConstants.P_UID)
                .operator(WhereOperator.AND).paramName(DaoConstants.P_UID).paramValue(uid).build());
        Map<String, Object> params = super.buildParams(whereClauses);
        String fullQuery = super.buildFullQuery(baseQuery, whereClauses, null, (String[]) null);
        return super.getNamedParameterJdbcTemplate().query(fullQuery, params, new ResultSetExtractor<AuditEventDb>() {
            @Override
            @Nullable
            public AuditEventDb extractData(ResultSet rs) throws SQLException, DataAccessException {
                return AuditEventDbExtractor.extractAuditEvent(rs, true);
            }
        });
    }

}
