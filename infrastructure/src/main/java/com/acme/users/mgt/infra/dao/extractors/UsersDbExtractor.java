package com.acme.users.mgt.infra.dao.extractors;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.acme.jga.users.mgt.dao.jdbc.utils.DaoConstants;
import com.acme.jga.users.mgt.dao.jdbc.utils.SQLExtractor;
import com.acme.jga.users.mgt.dto.users.UserStatus;
import com.acme.users.mgt.infra.dto.users.v1.UserDb;

public class UsersDbExtractor {
    private UsersDbExtractor() {
        // Private constructor for utility class
    }

    public static UserDb extractUser(ResultSet resultSet, boolean checkNext) throws SQLException {
        UserDb userDb = null;
        if (!checkNext || resultSet.next()) {
            userDb = UserDb.builder()
                    .email(SQLExtractor.extractString(resultSet, "email"))
                    .firstName(SQLExtractor.extractString(resultSet, "first_name"))
                    .id(SQLExtractor.extractLong(resultSet, DaoConstants.FIELD_ID))
                    .lastName(SQLExtractor.extractString(resultSet, "last_name"))
                    .login(SQLExtractor.extractString(resultSet, "login"))
                    .middleName(SQLExtractor.extractString(resultSet, "middle_name"))
                    .orgId(SQLExtractor.extractLong(resultSet, DaoConstants.FIELD_ORG_ID))
                    .status(UserStatus.fromIntValue(SQLExtractor.extractInteger(resultSet, "status")))
                    .tenantId(SQLExtractor.extractLong(resultSet, DaoConstants.FIELD_TENANT_ID))
                    .uid(SQLExtractor.extractString(resultSet, DaoConstants.FIELD_UID))
                    .build();
        }
        return userDb;
    }

}
