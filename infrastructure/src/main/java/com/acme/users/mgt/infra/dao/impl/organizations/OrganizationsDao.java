package com.acme.users.mgt.infra.dao.impl.organizations;

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
import com.acme.jga.users.mgt.dao.jdbc.spring.AbstractJdbcDaoSupport;
import com.acme.jga.users.mgt.dao.jdbc.utils.DaoConstants;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.dto.organizations.OrganizationStatus;
import com.acme.jga.users.mgt.dto.pagination.WhereClause;
import com.acme.jga.users.mgt.dto.pagination.WhereOperator;
import com.acme.users.mgt.infra.dao.api.organizations.IOrganizationsDao;
import com.acme.users.mgt.infra.dao.extractors.OrganizationDbExtractor;
import com.acme.users.mgt.infra.dto.organizations.v1.OrganizationDb;

@Repository
public class OrganizationsDao extends AbstractJdbcDaoSupport implements IOrganizationsDao {

	private static final String BASE_SELECT = "org_sel_base";

	public OrganizationsDao(DataSource ds, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		super(ds, namedParameterJdbcTemplate);
		super.loadQueryFilePath(new String[] { "organizations.properties" });
	}

	@Override
	public CompositeId createOrganization(OrganizationDb org) {
		String baseQuery = super.getQuery("org_create");
		KeyHolder keyHolder = new GeneratedKeyHolder();
		String uuid = DaoConstants.generatedUUID();
		MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
		mapSqlParameterSource.addValue(DaoConstants.P_TENANT_ID, org.getTenantId());
		mapSqlParameterSource.addValue(DaoConstants.P_UID, uuid);
		mapSqlParameterSource.addValue(DaoConstants.P_CODE, org.getCode());
		mapSqlParameterSource.addValue(DaoConstants.P_LABEL, org.getLabel());
		mapSqlParameterSource.addValue(DaoConstants.P_KIND, org.getKind().getCode());
		mapSqlParameterSource.addValue(DaoConstants.P_COUNTRY, org.getCountry());
		mapSqlParameterSource.addValue(DaoConstants.P_STATUS, org.getStatus().getCode());

		super.getNamedParameterJdbcTemplate().update(baseQuery, mapSqlParameterSource, keyHolder);
		Long generatedId = super.extractGeneratedId(keyHolder, DaoConstants.FIELD_ID);
		return new CompositeId(generatedId.longValue(), uuid);
	}

	@Override
	public OrganizationDb findOrganizationByTenantAndId(Long tenantId, Long id) {
		String baseQuery = super.getQuery(BASE_SELECT);
		List<WhereClause> whereClauses = new ArrayList<>();
		whereClauses.add(
				WhereClause.builder().expression(DaoConstants.FIELD_TENANT_ID + "=:" + DaoConstants.P_TENANT_ID)
						.operator(WhereOperator.AND)
						.paramName(DaoConstants.P_TENANT_ID).paramValue(tenantId).build());
		whereClauses.add(WhereClause.builder().expression(DaoConstants.FIELD_ID + "=:" + DaoConstants.P_ID)
				.operator(WhereOperator.AND)
				.paramName(DaoConstants.P_ID).paramValue(id).build());
		Map<String, Object> params = super.buildParams(whereClauses);
		String fullQuery = super.buildFullQuery(baseQuery, whereClauses, null, (String[]) null);
		return super.getNamedParameterJdbcTemplate().query(fullQuery, params, new ResultSetExtractor<OrganizationDb>() {
			@Override
			public OrganizationDb extractData(ResultSet rs) throws SQLException, DataAccessException {
				return OrganizationDbExtractor.extractOrganization(rs, true);
			}
		});
	}

	@Override
	public OrganizationDb findOrganizationByTenantAndUid(Long tenantId, String uid) {
		String baseQuery = super.getQuery(BASE_SELECT);
		List<WhereClause> whereClauses = new ArrayList<>();
		whereClauses.add(
				WhereClause.builder().expression(DaoConstants.FIELD_TENANT_ID + "=:" + DaoConstants.P_TENANT_ID)
						.operator(WhereOperator.AND)
						.paramName(DaoConstants.P_TENANT_ID).paramValue(tenantId).build());
		whereClauses.add(WhereClause.builder().expression(DaoConstants.FIELD_UID + "=:" + DaoConstants.P_UID)
				.operator(WhereOperator.AND)
				.paramName(DaoConstants.P_UID).paramValue(uid).build());
		Map<String, Object> params = super.buildParams(whereClauses);
		String fullQuery = super.buildFullQuery(baseQuery, whereClauses, null, (String[]) null);
		return super.getNamedParameterJdbcTemplate().query(fullQuery, params, new ResultSetExtractor<OrganizationDb>() {
			@Override
			public OrganizationDb extractData(ResultSet rs) throws SQLException, DataAccessException {
				return OrganizationDbExtractor.extractOrganization(rs, true);
			}
		});
	}

	@Override
	public Integer updateOrganization(Long tenantId, Long orgId, String code, String label, String country,
			OrganizationStatus status) {
		String baseQuery = super.getQuery("org_update");
		Map<String, Object> params = new HashMap<>();
		params.put(DaoConstants.P_TENANT_ID, tenantId);
		params.put(DaoConstants.P_ORG_ID, orgId);
		params.put(DaoConstants.P_CODE, code);
		params.put(DaoConstants.P_LABEL, label);
		params.put(DaoConstants.P_COUNTRY, country);
		params.put(DaoConstants.P_STATUS, status.getCode());
		return super.getNamedParameterJdbcTemplate().update(baseQuery, params);
	}

	@Override
	public Integer deleteOrganization(Long tenantId, Long orgId) {
		String baseQuery = super.getQuery("org_delete");
		Map<String, Object> params = new HashMap<>();
		params.put(DaoConstants.P_TENANT_ID, tenantId);
		params.put(DaoConstants.P_ID, orgId);
		return super.getNamedParameterJdbcTemplate().update(baseQuery, params);
	}

	@Override
	public List<OrganizationDb> findAllOrganizations(Long tenantId) {
		String baseQuery = super.getQuery(BASE_SELECT);
		return super.getNamedParameterJdbcTemplate().query(baseQuery, new RowMapper<OrganizationDb>() {
			@Override
			@Nullable
			public OrganizationDb mapRow(ResultSet rs, int rowNum) throws SQLException {
				return OrganizationDbExtractor.extractOrganization(rs, false);
			}
		});
	}

	@Override
	public Optional<Long> existsByCode(String code) {
		String baseQuery = super.getQuery("org_by_id_exists");
		Map<String, Object> params = new HashMap<>();
		params.put(DaoConstants.P_CODE, code);
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
	public List<OrganizationDb> findOrgsByIdList(List<Long> orgIds) {
		String baseQuery = super.getQuery(BASE_SELECT);
		List<WhereClause> whereClauses = new ArrayList<>();
		whereClauses.add(
				WhereClause.builder().expression(DaoConstants.FIELD_ID + " in (:" + DaoConstants.P_ID + ")")
						.operator(WhereOperator.AND)
						.paramName(DaoConstants.P_ID).paramValue(orgIds).build());
		Map<String, Object> params = super.buildParams(whereClauses);
		String fullQuery = super.buildFullQuery(baseQuery, whereClauses, null, (String[]) null);
		return super.getNamedParameterJdbcTemplate().query(fullQuery, params, new RowMapper<OrganizationDb>() {

			@Override
			@Nullable
			public OrganizationDb mapRow(ResultSet rs, int rowNum) throws SQLException {
				return OrganizationDbExtractor.extractOrganization(rs, false);
			}
		});
	}

	@Override
	public Integer deleteById(Long tenantId, Long orgId) {
		String baseQuery = super.getQuery("org_delete_by_id");
		Map<String, Object> params = new HashMap<>();
		params.put(DaoConstants.P_TENANT_ID, tenantId);
		params.put(DaoConstants.P_ID, orgId);
		return super.getNamedParameterJdbcTemplate().update(baseQuery, params);
	}

	@Override
	public Integer deleteUsersByOrganization(Long tenantId, Long orgId) {
		String baseQuery = super.getQuery("org_delete_users");
		Map<String, Object> params = new HashMap<>();
		params.put(DaoConstants.P_TENANT_ID, tenantId);
		params.put(DaoConstants.P_ORG_ID, orgId);
		return super.getNamedParameterJdbcTemplate().update(baseQuery, params);
	}

	@Override
	public Integer deleteSectorsByOrganization(Long tenantId, Long orgId) {
		String baseQuery = super.getQuery("org_delete_sectors");
		Map<String, Object> params = new HashMap<>();
		params.put(DaoConstants.P_TENANT_ID, tenantId);
		params.put(DaoConstants.P_ORG_ID, orgId);
		return super.getNamedParameterJdbcTemplate().update(baseQuery, params);
	}

}
