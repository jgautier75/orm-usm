package com.acme.users.mgt.infra.dao.impl.users;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;
import com.acme.jga.users.mgt.dao.jdbc.spring.AbstractJdbcDaoSupport;
import com.acme.jga.users.mgt.dao.jdbc.utils.DaoConstants;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.dto.pagination.WhereClause;
import com.acme.jga.users.mgt.dto.pagination.WhereOperator;
import com.acme.users.mgt.infra.dao.api.users.IUsersDao;
import com.acme.users.mgt.infra.dao.extractors.UsersDbExtractor;
import com.acme.users.mgt.infra.dto.users.v1.UserDb;

@Repository
public class UsersDao extends AbstractJdbcDaoSupport implements IUsersDao {

    private static final String BASE_SELECT = "user_sel_base";

    public UsersDao(DataSource ds, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        super(ds, namedParameterJdbcTemplate);
        super.loadQueryFilePath(new String[] { "users.properties" });
    }

    @Override
    public CompositeId createUser(UserDb userDb) {
        String baseQuery = super.getQuery("user_create");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String uuid = DaoConstants.generatedUUID();
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue(DaoConstants.P_UID, uuid);
        mapSqlParameterSource.addValue(DaoConstants.P_TENANT_ID, userDb.getTenantId());
        mapSqlParameterSource.addValue(DaoConstants.P_ORG_ID, userDb.getOrgId());
        mapSqlParameterSource.addValue(DaoConstants.P_LOGIN, userDb.getLogin());
        mapSqlParameterSource.addValue(DaoConstants.P_FIRST_NAME, userDb.getFirstName());
        mapSqlParameterSource.addValue(DaoConstants.P_LAST_NAME, userDb.getLastName());
        mapSqlParameterSource.addValue(DaoConstants.P_MIDDLE_NAME, userDb.getMiddleName());
        mapSqlParameterSource.addValue(DaoConstants.P_EMAIL, userDb.getEmail());
        mapSqlParameterSource.addValue(DaoConstants.P_STATUS, userDb.getStatus().getCode());
        super.getNamedParameterJdbcTemplate().update(baseQuery, mapSqlParameterSource, keyHolder);
        Long generatedId = super.extractGeneratedId(keyHolder, DaoConstants.FIELD_ID);
        return new CompositeId(generatedId.longValue(), uuid);
    }

    @Override
    public UserDb findById(Long tenantId, Long orgId, Long id) {
        String baseQuery = super.getQuery(BASE_SELECT);
        List<WhereClause> whereClauses = new ArrayList<>();
        whereClauses.add(
                WhereClause.builder().expression(DaoConstants.FIELD_TENANT_ID + "=:" + DaoConstants.P_TENANT_ID)
                        .operator(WhereOperator.AND)
                        .paramName(DaoConstants.P_TENANT_ID).paramValue(tenantId).build());
        whereClauses.add(
                WhereClause.builder().expression(DaoConstants.FIELD_ORG_ID + "=:" + DaoConstants.P_ORG_ID)
                        .operator(WhereOperator.AND)
                        .paramName(DaoConstants.P_ORG_ID).paramValue(orgId).build());
        whereClauses.add(WhereClause.builder().expression(DaoConstants.FIELD_ID + "=:" + DaoConstants.P_ID)
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_ID).paramValue(id).build());
        Map<String, Object> params = super.buildParams(whereClauses);
        String fullQuery = super.buildFullQuery(baseQuery, whereClauses, null, (String[]) null);
        return super.getNamedParameterJdbcTemplate().query(fullQuery, params, new ResultSetExtractor<UserDb>() {
            @Override
            public UserDb extractData(ResultSet rs) throws SQLException, DataAccessException {
                return UsersDbExtractor.extractUser(rs, true);
            }
        });
    }

    @Override
    public UserDb findByUid(Long tenantId, Long orgId, String uid) {
        String baseQuery = super.getQuery(BASE_SELECT);
        List<WhereClause> whereClauses = new ArrayList<>();
        whereClauses.add(
                WhereClause.builder().expression(DaoConstants.FIELD_TENANT_ID + "=:" + DaoConstants.P_TENANT_ID)
                        .operator(WhereOperator.AND)
                        .paramName(DaoConstants.P_TENANT_ID).paramValue(tenantId).build());
        whereClauses.add(
                WhereClause.builder().expression(DaoConstants.FIELD_ORG_ID + "=:" + DaoConstants.P_ORG_ID)
                        .operator(WhereOperator.AND)
                        .paramName(DaoConstants.P_ORG_ID).paramValue(orgId).build());
        whereClauses.add(WhereClause.builder().expression(DaoConstants.FIELD_UID + "=:" + DaoConstants.P_UID)
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_UID).paramValue(uid).build());
        Map<String, Object> params = super.buildParams(whereClauses);
        String fullQuery = super.buildFullQuery(baseQuery, whereClauses, null, (String[]) null);
        return super.getNamedParameterJdbcTemplate().query(fullQuery, params, new ResultSetExtractor<UserDb>() {
            @Override
            public UserDb extractData(ResultSet rs) throws SQLException, DataAccessException {
                return UsersDbExtractor.extractUser(rs, true);
            }
        });
    }

    @Override
    public Integer updateUser(UserDb userDb) {
        String baseQuery = super.getQuery("user_update");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_TENANT_ID, userDb.getTenantId());
        params.put(DaoConstants.P_ORG_ID, userDb.getOrgId());
        params.put(DaoConstants.P_ID, userDb.getId());
        params.put(DaoConstants.P_LOGIN, userDb.getLogin());
        params.put(DaoConstants.P_EMAIL, userDb.getEmail());
        params.put(DaoConstants.P_FIRST_NAME, userDb.getFirstName());
        params.put(DaoConstants.P_LAST_NAME, userDb.getLastName());
        params.put(DaoConstants.P_MIDDLE_NAME, userDb.getMiddleName());
        params.put(DaoConstants.P_STATUS, userDb.getStatus().getCode());
        return super.getNamedParameterJdbcTemplate().update(baseQuery, params);
    }

    @Override
    public Integer deleteUser(Long tenantId, Long orgId, Long userId) {
        String baseQuery = super.getQuery("user_delete");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_TENANT_ID, tenantId);
        params.put(DaoConstants.P_ORG_ID, orgId);
        params.put(DaoConstants.P_ID, userId);
        return super.getNamedParameterJdbcTemplate().update(baseQuery, params);
    }

    @Override
    public Optional<Long> emailExists(String email) {
        String baseQuery = super.getQuery("user_id_by_email");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_EMAIL, email);
        Long userId = super.getNamedParameterJdbcTemplate().query(baseQuery, params, new ResultSetExtractor<Long>() {
            @Override
            public Long extractData(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return rs.getLong(1);
                } else {
                    return null;
                }
            }
        });
        return Optional.ofNullable(userId);
    }

    @Override
    public Optional<Long> loginExists(String login) {
        String baseQuery = super.getQuery("user_id_by_login");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_LOGIN, login);
        Long userId = super.getNamedParameterJdbcTemplate().query(baseQuery, params, new ResultSetExtractor<Long>() {
            @Override
            public Long extractData(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return rs.getLong(1);
                } else {
                    return null;
                }
            }
        });
        return Optional.ofNullable(userId);
    }

    @Override
    public List<UserDb> findUsers(Long tenantId, Long orgId) {
        String baseQuery = super.getQuery(BASE_SELECT);
        List<WhereClause> whereClauses = new ArrayList<>();
        if (!ObjectUtils.isEmpty(tenantId)) {
            whereClauses.add(WhereClause.builder()
                    .expression(DaoConstants.FIELD_TENANT_ID + "=:" + DaoConstants.P_TENANT_ID)
                    .operator(WhereOperator.AND)
                    .paramName(DaoConstants.P_TENANT_ID)
                    .paramValue(tenantId)
                    .build());
        }
        if (!ObjectUtils.isEmpty(orgId)) {
            whereClauses.add(WhereClause.builder()
                    .expression(DaoConstants.FIELD_ORG_ID + "=:" + DaoConstants.P_ORG_ID)
                    .operator(WhereOperator.AND)
                    .paramName(DaoConstants.P_ORG_ID)
                    .paramValue(orgId)
                    .build());
        }
        Map<String, Object> params = super.buildParams(whereClauses);
        String fullQuery = super.buildFullQuery(baseQuery, whereClauses, null, (String[]) null);
        return super.getNamedParameterJdbcTemplate().query(fullQuery, params, new RowMapper<UserDb>() {
            @Override
            @Nullable
            public UserDb mapRow(ResultSet rs, int rowNum) throws SQLException {
                return UsersDbExtractor.extractUser(rs, false);
            }
        });
    }

}
