package com.acme.users.mgt.infra.dao.api.users;

import java.util.List;
import java.util.Optional;

import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.users.mgt.infra.dto.users.v1.UserDb;

public interface IUsersDao {

	CompositeId createUser(UserDb userDb);

	UserDb findById(Long tenantId, Long orgId, Long id);

	UserDb findByUid(Long tenantId, Long orgId, String uid);

	Integer updateUser(UserDb userDb);

	Integer deleteUser(Long tenantId, Long orgId, Long userId);

	Optional<Long> emailExists(String email);

	Optional<Long> loginExists(String login);

	List<UserDb> findUsers(Long tenantId, Long orgId);

}
