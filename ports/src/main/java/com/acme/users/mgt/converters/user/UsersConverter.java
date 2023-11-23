package com.acme.users.mgt.converters.user;

import org.springframework.stereotype.Component;
import com.acme.jga.users.mgt.domain.users.v1.User;
import com.acme.jga.users.mgt.domain.users.v1.UserCommons;
import com.acme.jga.users.mgt.domain.users.v1.UserCredentials;
import com.acme.users.mgt.dto.port.organizations.v1.OrganizationLightDto;
import com.acme.users.mgt.dto.port.users.v1.UserDisplayDto;
import com.acme.users.mgt.dto.port.users.v1.UserDto;

@Component
public class UsersConverter {

    public User convertUserDtoToDomain(UserDto userDto) {
        User user = null;
        if (userDto != null) {
            user = new User();
            user.setUid(userDto.getUid());
            if (userDto.getCommons() != null) {
                UserCommons userCommons = UserCommons.builder()
                        .firstName(userDto.getCommons().getFirstName())
                        .lastName(userDto.getCommons().getLastName())
                        .middleName(userDto.getCommons().getMiddleName())
                        .build();
                user.setCommons(userCommons);
            }
            if (userDto.getCredentials() != null) {
                UserCredentials userCredentials = UserCredentials.builder()
                        .email(userDto.getCredentials().getEmail())
                        .login(userDto.getCredentials().getLogin())
                        .build();
                user.setCredentials(userCredentials);
            }
            user.setStatus(userDto.getStatus());
        }
        return user;
    }

    public UserDisplayDto convertUserDomainToDisplay(User user) {
        UserDisplayDto displayDto = null;
        if (user != null) {
            displayDto = new UserDisplayDto();
            displayDto.setUid(user.getUid());
            if (user.getCommons() != null) {
                displayDto.setFirstName(user.getCommons().getFirstName());
                displayDto.setLastName(user.getCommons().getLastName());
                displayDto.setMiddleName(user.getCommons().getMiddleName());
            }
            if (user.getCredentials() != null) {
                displayDto.setEmail(user.getCredentials().getEmail());
                displayDto.setLogin(user.getCredentials().getLogin());
            }
            if (user.getOrganization() != null) {
                OrganizationLightDto orgDto = new OrganizationLightDto();
                orgDto.setKind(user.getOrganization().getCommons().getKind());
                orgDto.setLabel(user.getOrganization().getCommons().getLabel());
                orgDto.setStatus(user.getOrganization().getCommons().getStatus());
                orgDto.setUid(user.getOrganization().getUid());
                displayDto.setOrganization(orgDto);
            }
        }

        return displayDto;
    }

}
