package com.acme.users.mgt.services.impl.user;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.acme.jga.users.mgt.domain.users.v1.User;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.users.mgt.converters.organization.OrganizationsConverter;
import com.acme.users.mgt.converters.user.UsersConverter;
import com.acme.users.mgt.dto.port.organizations.v1.OrganizationLightDto;
import com.acme.users.mgt.dto.port.shared.UidDto;
import com.acme.users.mgt.dto.port.users.v1.UserDisplayDto;
import com.acme.users.mgt.dto.port.users.v1.UserDto;
import com.acme.users.mgt.dto.port.users.v1.UsersDisplayListDto;
import com.acme.users.mgt.services.api.users.IUserPortService;
import com.acme.users.mgt.services.users.api.IUserDomainService;
import com.acme.users.mgt.validation.ValidationException;
import com.acme.users.mgt.validation.ValidationResult;
import com.acme.users.mgt.validation.users.UsersValidationEngine;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserPortService implements IUserPortService {
    private final IUserDomainService userDomainService;
    private final UsersConverter usersConverter;
    private final OrganizationsConverter organizationsConverter;
    private final UsersValidationEngine usersValidationEngine;

    @Override
    public UidDto createUser(String tenantUid, String orgUid, UserDto userDto) throws FunctionalException {

        // Validate payload
        ValidationResult validationResult = usersValidationEngine.validate(userDto);
        if (!validationResult.isSuccess()) {
            throw new ValidationException(validationResult.getErrors());
        }

        User user = usersConverter.convertUserDtoToDomain(userDto);
        CompositeId compositeId = userDomainService.createUser(tenantUid, orgUid, user);
        return new UidDto(compositeId.getUid());
    }

    @Override
    public void updateUser(String tenantUid, String orgUid, String userUid, UserDto userDto)
            throws FunctionalException {
        userDto.setUid(userUid);
        // Validate payload
        ValidationResult validationResult = usersValidationEngine.validate(userDto);
        if (!validationResult.isSuccess()) {
            throw new ValidationException(validationResult.getErrors());
        }

        User user = usersConverter.convertUserDtoToDomain(userDto);
        userDomainService.updateUser(tenantUid, orgUid, user);
    }

    @Override
    public UsersDisplayListDto findUsers(String tenantUid, String orgUid) throws FunctionalException {
        List<User> users = userDomainService.findUsers(tenantUid, orgUid);
        List<UserDisplayDto> displayDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(users)) {
            for (User user : users) {
                OrganizationLightDto orgLightDto = organizationsConverter
                        .convertOrganizationToLightOrgDto(user.getOrganization());
                UserDisplayDto userDisplayDto = usersConverter.convertUserDomainToDisplay(user);
                userDisplayDto.setOrganization(orgLightDto);
                displayDtos.add(userDisplayDto);
            }
        }
        return new UsersDisplayListDto(displayDtos);
    }

    @Override
    public void deleteUser(String tenantUid, String orgUid, String userUid) throws FunctionalException {
        userDomainService.deleteUser(tenantUid, orgUid, userUid);
    }

}
