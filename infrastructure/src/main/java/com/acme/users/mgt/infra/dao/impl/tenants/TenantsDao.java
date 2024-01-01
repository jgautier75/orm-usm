package com.acme.users.mgt.infra.dao.impl.tenants;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.acme.jga.users.mgt.dao.jdbc.spring.AbstractJdbcDaoSupport;
import com.acme.jga.users.mgt.dao.jdbc.utils.DaoConstants;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.dto.pagination.WhereClause;
import com.acme.jga.users.mgt.dto.pagination.WhereOperator;
import com.acme.users.mgt.infra.dao.api.tenants.ITenantsDao;
import com.acme.users.mgt.infra.dao.extractors.TenantsDbExtractor;
import com.acme.users.mgt.infra.dto.tenants.v1.TenantDb;

@Repository
public class TenantsDao extends AbstractJdbcDaoSupport implements ITenantsDao {

    private static final String BASE_SELECT = "tenant_sel_base";

    public TenantsDao(DataSource ds, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        super(ds, namedParameterJdbcTemplate);
        super.loadQueryFilePath(TenantsDao.class.getClassLoader(), new String[] { "tenants.properties" });
    }

    @Override
    public TenantDb findById(Long id) {
        String baseQuery = super.getQuery(BASE_SELECT);
        List<WhereClause> whereClauses = new ArrayList<>();
        whereClauses.add(WhereClause.builder().expression(DaoConstants.FIELD_ID + "=:" + DaoConstants.P_ID)
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_ID).paramValue(id).build());
        Map<String, Object> params = super.buildParams(whereClauses);
        String fullQuery = super.buildFullQuery(baseQuery, whereClauses, null, (String[]) null);
        return super.getNamedParameterJdbcTemplate().query(fullQuery, params, new ResultSetExtractor<TenantDb>() {
            @Override
            public TenantDb extractData(ResultSet rs) throws SQLException, DataAccessException {
                return TenantsDbExtractor.extractTenant(rs, true);
            }
        });
    }

    @Override
    public TenantDb findByUid(String uid) {
        String baseQuery = super.getQuery(BASE_SELECT);
        List<WhereClause> whereClauses = new ArrayList<>();
        whereClauses.add(WhereClause.builder().expression(DaoConstants.FIELD_UID + "=:" + DaoConstants.P_UID)
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_UID).paramValue(uid).build());
        Map<String, Object> params = super.buildParams(whereClauses);
        String fullQuery = super.buildFullQuery(baseQuery, whereClauses, null, (String[]) null);
        return super.getNamedParameterJdbcTemplate().query(fullQuery, params, new ResultSetExtractor<TenantDb>() {
            @Override
            public TenantDb extractData(ResultSet rs) throws SQLException, DataAccessException {
                return TenantsDbExtractor.extractTenant(rs, true);
            }
        });
    }

    @Override
    public TenantDb findByCode(String code) {
        String baseQuery = super.getQuery(BASE_SELECT);
        List<WhereClause> whereClauses = new ArrayList<>();
        whereClauses.add(WhereClause.builder().expression(DaoConstants.FIELD_CODE + "=:" + DaoConstants.P_CODE)
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_CODE).paramValue(code).build());
        Map<String, Object> params = super.buildParams(whereClauses);
        String fullQuery = super.buildFullQuery(baseQuery, whereClauses, null, (String[]) null);
        return super.getNamedParameterJdbcTemplate().query(fullQuery, params, new ResultSetExtractor<TenantDb>() {
            @Override
            public TenantDb extractData(ResultSet rs) throws SQLException, DataAccessException {
                return TenantsDbExtractor.extractTenant(rs, true);
            }
        });
    }

    @Override
    public CompositeId createTenant(String code, String label) {
        String baseQuery = super.getQuery("tenant_create");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String uuid = DaoConstants.generatedUUID();
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue(DaoConstants.P_UID, uuid);
        mapSqlParameterSource.addValue(DaoConstants.P_CODE, code);
        mapSqlParameterSource.addValue(DaoConstants.P_LABEL, label);
        super.getNamedParameterJdbcTemplate().update(baseQuery, mapSqlParameterSource, keyHolder);
        Long generatedId = super.extractGeneratedId(keyHolder, DaoConstants.FIELD_ID);
        return new CompositeId(generatedId.longValue(), uuid);
    }

    @Override
    public Integer updateTenant(Long tenantId, String code, String label) {
        String baseQuery = super.getQuery("tenant_update");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_ID, tenantId);
        params.put(DaoConstants.P_CODE, code);
        params.put(DaoConstants.P_LABEL, label);
        return super.getNamedParameterJdbcTemplate().update(baseQuery, params);
    }

    @Override
    public Integer deleteTenant(Long tenantId) {
        String baseQuery = super.getQuery("tenant_delete_root");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_ID, tenantId);
        return super.getNamedParameterJdbcTemplate().update(baseQuery, params);
    }

    @Override
    public Boolean existsByCode(String code) {
        String baseQuery = super.getQuery("tenant_exists_by_code");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_CODE, code);
        return super.executeExists(baseQuery, params);
    }

    @Override
    public List<TenantDb> findAllTenants() {
        String baseQuery = super.getQuery(BASE_SELECT);
        return super.getNamedParameterJdbcTemplate().query(baseQuery, new RowMapper<TenantDb>() {
            @Override
            @Nullable
            public TenantDb mapRow(ResultSet rs, int rowNum) throws SQLException {
                return TenantsDbExtractor.extractTenant(rs, false);
            }

        });
    }

    @Override
    public Integer deleteUsersByTenantId(Long tenantId) {
        String baseQuery = super.getQuery("tenant_delete_users");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_TENANT_ID, tenantId);
        return super.getNamedParameterJdbcTemplate().update(baseQuery, params);
    }

    @Override
    public Integer deleteOrganizationsByTenantId(Long tenantId) {
        String baseQuery = super.getQuery("tenant_delete_orgs");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_TENANT_ID, tenantId);
        return super.getNamedParameterJdbcTemplate().update(baseQuery, params);
    }

    @Override
    public Integer deleteSectorsByTenantId(Long tenantId) {
        String baseQuery = super.getQuery("tenant_delete_sectors");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_TENANT_ID, tenantId);
        return super.getNamedParameterJdbcTemplate().update(baseQuery, params);
    }

}
