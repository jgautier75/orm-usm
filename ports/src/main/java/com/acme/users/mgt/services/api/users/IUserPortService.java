package com.acme.users.mgt.services.api.users;

import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.users.mgt.dto.port.shared.UidDto;
import com.acme.users.mgt.dto.port.users.v1.UserDisplayDto;
import com.acme.users.mgt.dto.port.users.v1.UserDto;
import com.acme.users.mgt.dto.port.users.v1.UsersDisplayListDto;

public interface IUserPortService {

    UidDto createUser(String tenantUid, String orgUid, UserDto userDto) throws FunctionalException;

    void updateUser(String tenantUid, String orgUid, String userUid, UserDto userDto) throws FunctionalException;

    UsersDisplayListDto findUsers(String tenantUid, String orgUid) throws FunctionalException;

    Integer deleteUser(String tenantUid, String orgUid, String userUid) throws FunctionalException;

    UserDisplayDto findUser(String tenantUid, String orgUid, String userUid) throws FunctionalException;

}
