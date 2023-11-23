package com.acme.users.mgt.infra.services.api.users;

import java.util.List;
import java.util.Optional;

import com.acme.jga.users.mgt.domain.users.v1.User;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.exceptions.FunctionalException;

public interface IUsersInfraService {

    CompositeId createUser(User user) throws FunctionalException;

    Optional<Long> emailUsed(String email);

    Optional<Long> loginUsed(String login);

    Integer updateUser(User user);

    User findByUid(Long tenantId, Long orgId, String userUid) throws FunctionalException;

    List<User> findUsers(Long tenantId, Long orgId);

    Integer deleteUser(Long tenantId, Long orgId, Long userId);
}
