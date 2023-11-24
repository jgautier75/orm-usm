package com.acme.users.mgt.infra.dao.extractors;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.acme.jga.users.mgt.dao.jdbc.utils.DaoConstants;
import com.acme.jga.users.mgt.dao.jdbc.utils.SQLExtractor;
import com.acme.users.mgt.infra.dto.sectors.v1.SectorDb;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SectorDbExtractor {
    public static SectorDb extractSector(ResultSet resultSet, boolean checkNext) throws SQLException {
        SectorDb sectorDb = null;
        if (!checkNext || resultSet.next()) {
            sectorDb = SectorDb.builder()
                    .code(SQLExtractor.extractString(resultSet, DaoConstants.FIELD_CODE))
                    .id(SQLExtractor.extractLong(resultSet, DaoConstants.FIELD_ID))
                    .label(SQLExtractor.extractString(resultSet, DaoConstants.FIELD_LABEL))
                    .orgId(SQLExtractor.extractLong(resultSet, DaoConstants.FIELD_ORG_ID))
                    .parentId(SQLExtractor.extractLong(resultSet, "parent_id"))
                    .root(SQLExtractor.extractBoolean(resultSet, "root"))
                    .tenantId(SQLExtractor.extractLong(resultSet, DaoConstants.FIELD_TENANT_ID))
                    .uid(SQLExtractor.extractString(resultSet, DaoConstants.FIELD_UID)).build();
        }
        return sectorDb;
    }
}
