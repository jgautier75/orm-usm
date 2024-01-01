package com.acme.users.mgt.infra.dao.extractors;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.acme.jga.users.mgt.dao.jdbc.utils.DaoConstants;
import com.acme.jga.users.mgt.dao.jdbc.utils.SQLExtractor;
import com.acme.jga.users.mgt.dto.organizations.OrganizationKind;
import com.acme.jga.users.mgt.dto.organizations.OrganizationStatus;
import com.acme.users.mgt.infra.dto.organizations.v1.OrganizationDb;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrganizationDbExtractor {
	public static OrganizationDb extractOrganization(ResultSet resultSet, boolean checkNext) throws SQLException {
		OrganizationDb org = null;
		if (!checkNext || resultSet.next()) {
			org = OrganizationDb.builder().country(SQLExtractor.extractString(resultSet, "country"))
					.id(SQLExtractor.extractLong(resultSet, DaoConstants.FIELD_ID))
					.code(SQLExtractor.extractString(resultSet, DaoConstants.FIELD_CODE))
					.kind(OrganizationKind.fromIntValue(SQLExtractor.extractInteger(resultSet, "kind")))
					.label(SQLExtractor.extractString(resultSet, DaoConstants.FIELD_LABEL))
					.status(OrganizationStatus.fromIntValue(SQLExtractor.extractInteger(resultSet, "status")))
					.tenantId(SQLExtractor.extractLong(resultSet, DaoConstants.FIELD_TENANT_ID))
					.uid(SQLExtractor.extractString(resultSet, DaoConstants.FIELD_UID)).build();
		}
		return org;
	}
}
