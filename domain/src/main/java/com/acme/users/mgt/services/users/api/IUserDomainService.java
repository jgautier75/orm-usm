package com.acme.users.mgt.services.users.api;

import java.util.List;

import com.acme.jga.users.mgt.domain.users.v1.User;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.exceptions.FunctionalException;

public interface IUserDomainService {

    CompositeId createUser(String tenantUid, String orgUid, User user) throws FunctionalException;

    void updateUser(String tenantUid, String orgUid, User user) throws FunctionalException;

    List<User> findUsers(String tenantUid, String orgUid) throws FunctionalException;

    Integer deleteUser(String tenantUid, String orgUid, String userUid) throws FunctionalException;

    User findByUid(String tenantUid, String orgUid, String userUid) throws FunctionalException;

}
