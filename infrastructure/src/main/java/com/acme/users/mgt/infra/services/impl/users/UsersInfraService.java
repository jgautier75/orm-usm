package com.acme.users.mgt.infra.services.impl.users;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import com.acme.jga.users.mgt.domain.users.v1.User;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.users.mgt.infra.converters.UsersInfraConverter;
import com.acme.users.mgt.infra.dao.api.users.IUsersDao;
import com.acme.users.mgt.infra.dto.users.v1.UserDb;
import com.acme.users.mgt.infra.services.api.users.IUsersInfraService;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UsersInfraService implements IUsersInfraService {
    private final IUsersDao usersDao;
    private final UsersInfraConverter usersInfraConverter;

    @Transactional
    @Override
    public CompositeId createUser(User user) throws FunctionalException {
        UserDb userDb = usersInfraConverter.convertUserToDb(user);
        return usersDao.createUser(userDb);
    }

    @Override
    public Optional<Long> emailUsed(String email) {
        return usersDao.emailExists(email);
    }

    @Override
    public Optional<Long> loginUsed(String login) {
        return usersDao.loginExists(login);
    }

    @Override
    public Integer updateUser(User user) {
        UserDb userDb = usersInfraConverter.convertUserToDb(user);
        return usersDao.updateUser(userDb);
    }

    @Override
    public User findByUid(Long tenantId, Long orgId, String userUid) throws FunctionalException {
        UserDb userDb = usersDao.findByUid(tenantId, orgId, userUid);
        return usersInfraConverter.convertUserDbToUser(userDb);
    }

    @Override
    public List<User> findUsers(Long tenantId, Long orgId) {
        List<UserDb> users = usersDao.findUsers(tenantId, orgId);
        List<User> domUsers = new ArrayList<>();
        if (!CollectionUtils.isEmpty(users)) {
            for (UserDb userDb : users) {
                domUsers.add(usersInfraConverter.convertUserDbToUser(userDb));
            }
        }
        return domUsers;
    }

    @Override
    public Integer deleteUser(Long tenantId, Long orgId, Long userId) {
        return usersDao.deleteUser(tenantId, orgId, userId);
    }

}
