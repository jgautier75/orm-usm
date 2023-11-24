package com.acme.users.mgt.infra.dao.extractors;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.acme.jga.users.mgt.dao.jdbc.utils.DaoConstants;
import com.acme.jga.users.mgt.dao.jdbc.utils.SQLExtractor;
import com.acme.users.mgt.infra.dto.tenants.v1.TenantDb;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TenantsDbExtractor {
    public static TenantDb extractTenant(ResultSet resultSet, boolean checkNext) throws SQLException {
        TenantDb tenant = null;
        if (!checkNext || resultSet.next()) {
            tenant = TenantDb.builder()
                    .id(SQLExtractor.extractLong(resultSet, DaoConstants.FIELD_ID))
                    .uid(SQLExtractor.extractString(resultSet, DaoConstants.FIELD_UID))
                    .code(SQLExtractor.extractString(resultSet, DaoConstants.FIELD_CODE))
                    .label(SQLExtractor.extractString(resultSet, DaoConstants.FIELD_LABEL))
                    .build();
        }
        return tenant;
    }

}
