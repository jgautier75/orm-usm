package com.acme.users.mgt.infra.dao.impl.sectors;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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

import com.acme.jga.users.mgt.dao.jdbc.spring.AbstractJdbcDaoSupport;
import com.acme.jga.users.mgt.dao.jdbc.utils.DaoConstants;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.dto.pagination.WhereClause;
import com.acme.jga.users.mgt.dto.pagination.WhereOperator;
import com.acme.users.mgt.infra.dao.api.sectors.ISectorsDao;
import com.acme.users.mgt.infra.dao.extractors.SectorDbExtractor;
import com.acme.users.mgt.infra.dto.sectors.v1.SectorDb;

@Repository
public class SectorsDao extends AbstractJdbcDaoSupport implements ISectorsDao {

    public SectorsDao(DataSource ds, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        super(ds, namedParameterJdbcTemplate);
        super.loadQueryFilePath(new String[] { "sectors.properties" });
    }

    @Override
    public List<SectorDb> findSectorsByOrgId(Long tenantId, Long orgId) {
        String baseQuery = super.getQuery("sector_base");
        List<WhereClause> whereClauses = new ArrayList<>();
        whereClauses.add(WhereClause.builder()
                .expression(DaoConstants.FIELD_TENANT_ID + "=:" + DaoConstants.P_TENANT_ID)
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_TENANT_ID)
                .paramValue(tenantId)
                .build());
        whereClauses.add(WhereClause.builder()
                .expression(DaoConstants.FIELD_ORG_ID + "=:" + DaoConstants.P_ORG_ID)
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_ORG_ID)
                .paramValue(orgId)
                .build());
        Map<String, Object> params = super.buildParams(whereClauses);
        String fullQuery = super.buildFullQuery(baseQuery, whereClauses, Collections.emptyList(), (String[]) null);
        return super.getNamedParameterJdbcTemplate().query(fullQuery, params, new RowMapper<SectorDb>() {
            @Override
            @Nullable
            public SectorDb mapRow(ResultSet rs, int rowNum) throws SQLException {
                return SectorDbExtractor.extractSector(rs, false);
            }

        });
    }

    @Override
    public CompositeId createSector(Long tenantId, Long orgId, SectorDb sectorDb) {
        String baseQuery = super.getQuery("sector_create");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String uuid = DaoConstants.generatedUUID();
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue(DaoConstants.P_TENANT_ID, tenantId);
        mapSqlParameterSource.addValue(DaoConstants.P_ORG_ID, orgId);
        mapSqlParameterSource.addValue(DaoConstants.P_UID, uuid);
        mapSqlParameterSource.addValue(DaoConstants.P_CODE, sectorDb.getCode());
        mapSqlParameterSource.addValue(DaoConstants.P_LABEL, sectorDb.getLabel());
        mapSqlParameterSource.addValue("pRoot", sectorDb.isRoot());
        mapSqlParameterSource.addValue(DaoConstants.P_PARENT_ID, sectorDb.getParentId());
        super.getNamedParameterJdbcTemplate().update(baseQuery, mapSqlParameterSource, keyHolder);
        Long generatedId = super.extractGeneratedId(keyHolder, DaoConstants.FIELD_ID);
        return new CompositeId(generatedId.longValue(), uuid);
    }

    @Override
    public SectorDb findByUid(Long tenantId, Long orgId, String uid) {
        String baseQuery = super.getQuery("sector_base");
        List<WhereClause> whereClauses = new ArrayList<>();
        whereClauses.add(WhereClause.builder()
                .expression(DaoConstants.FIELD_TENANT_ID + "=:" + DaoConstants.P_TENANT_ID)
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_TENANT_ID)
                .paramValue(tenantId)
                .build());
        whereClauses.add(WhereClause.builder()
                .expression(DaoConstants.FIELD_ORG_ID + "=:" + DaoConstants.P_ORG_ID)
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_ORG_ID)
                .paramValue(orgId)
                .build());
        whereClauses.add(WhereClause.builder()
                .expression(DaoConstants.FIELD_UID + "=:" + DaoConstants.P_UID)
                .operator(WhereOperator.AND)
                .paramName(DaoConstants.P_UID)
                .paramValue(uid)
                .build());
        Map<String, Object> params = super.buildParams(whereClauses);
        String fullQuery = super.buildFullQuery(baseQuery, whereClauses, Collections.emptyList(), (String[]) null);
        return super.getNamedParameterJdbcTemplate().query(fullQuery, params, new ResultSetExtractor<SectorDb>() {
            @Override
            @Nullable
            public SectorDb extractData(ResultSet rs) throws SQLException, DataAccessException {
                return SectorDbExtractor.extractSector(rs, true);
            }
        });
    }

    @Override
    public Optional<Long> existsByCode(String code) {
        String baseQuery = super.getQuery("sector_exists_by_code");
        Map<String, Object> params = new HashMap<>();
        params.put(DaoConstants.P_CODE, code);
        Long sectorId = super.getNamedParameterJdbcTemplate().query(baseQuery, params, new ResultSetExtractor<Long>() {
            @Override
            public Long extractData(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return rs.getLong(1);
                } else {
                    return null;
                }
            }
        });
        return Optional.ofNullable(sectorId);
    }

}
