package com.acme.users.mgt.infra.converters;

import org.springframework.stereotype.Component;

import com.acme.jga.users.mgt.domain.users.v1.User;
import com.acme.jga.users.mgt.domain.users.v1.UserCommons;
import com.acme.jga.users.mgt.domain.users.v1.UserCredentials;
import com.acme.users.mgt.infra.dto.users.v1.UserDb;

@Component
public class UsersInfraConverter {

    public UserDb convertUserToDb(User user) {
        UserDb userDb = null;
        if (user != null) {
            userDb = UserDb.builder()
                    .email(user.getCredentials().getEmail())
                    .firstName(user.getCommons().getFirstName())
                    .lastName(user.getCommons().getLastName())
                    .login(user.getCredentials().getEmail())
                    .middleName(user.getCommons().getMiddleName())
                    .orgId(user.getOrganizationId())
                    .status(user.getStatus())
                    .tenantId(user.getTenantId())
                    .id(user.getId())
                    .build();
            userDb.setStatus(user.getStatus());
        }

        return userDb;
    }

    public User convertUserDbToUser(UserDb userDb) {
        User user = null;
        if (userDb != null) {
            UserCommons userCommons = UserCommons.builder()
                    .firstName(userDb.getFirstName())
                    .lastName(userDb.getLastName())
                    .middleName(userDb.getMiddleName())
                    .build();
            UserCredentials userCredentials = UserCredentials.builder()
                    .email(userDb.getEmail())
                    .login(userDb.getLogin())
                    .build();
            user = User.builder()
                    .commons(userCommons)
                    .credentials(userCredentials)
                    .id(userDb.getId())
                    .organizationId(userDb.getOrgId())
                    .status(userDb.getStatus())
                    .tenantId(userDb.getTenantId())
                    .uid(userDb.getUid())
                    .build();
        }
        return user;
    }

}
