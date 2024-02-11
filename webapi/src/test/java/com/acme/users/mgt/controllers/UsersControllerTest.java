package com.acme.users.mgt.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.acme.jga.users.mgt.dto.organizations.OrganizationKind;
import com.acme.jga.users.mgt.dto.organizations.OrganizationStatus;
import com.acme.users.mgt.config.AppDebuggingConfig;
import com.acme.users.mgt.config.AppGenericConfig;
import com.acme.users.mgt.config.MicrometerPrometheus;
import com.acme.users.mgt.config.OpenTelemetryTestConfig;
import com.acme.users.mgt.dto.port.organizations.v1.OrganizationLightDto;
import com.acme.users.mgt.dto.port.shared.UidDto;
import com.acme.users.mgt.dto.port.users.v1.UserCommonsDto;
import com.acme.users.mgt.dto.port.users.v1.UserCredentialsDto;
import com.acme.users.mgt.dto.port.users.v1.UserDisplayDto;
import com.acme.users.mgt.dto.port.users.v1.UserDto;
import com.acme.users.mgt.dto.port.users.v1.UsersDisplayListDto;
import com.acme.users.mgt.logging.services.api.ILogService;
import com.acme.users.mgt.services.api.users.IUserPortService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = UsersController.class)
@Import(value = {OpenTelemetryTestConfig.class})
class UsersControllerTest {
        private static final String TENANT_UID = UUID.randomUUID().toString();
        private static final String ORG_UID = UUID.randomUUID().toString();
        @MockBean
        private IUserPortService userPortService;
        @MockBean
        private ILogService logService;
        @MockBean
        private AppGenericConfig appGenericConfig;
        @MockBean
        private AppDebuggingConfig appDebuggingConfig;
        @MockBean
        private MicrometerPrometheus micrometerPrometheus;
        @Autowired
        private MockMvc mockMvc;

        @Test
        void createUser() throws Exception {
                // GIVEN
                UidDto uidDto = new UidDto(UUID.randomUUID().toString());
                UserDto userDto = mockUserDto();

                ObjectMapper mapper = new ObjectMapper();
                String userJson = mapper.writeValueAsString(userDto);

                // WHEN
                Mockito.when(userPortService.createUser(Mockito.any(), Mockito.any(), Mockito.any()))
                                .thenReturn(uidDto);

                // THEN
                String usersUri = "/api/v1/tenants/" + TENANT_UID + "/organizations/" + ORG_UID + "/users";
                mockMvc.perform(post(usersUri)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(userJson)
                                .accept(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8))
                                .andDo(print())
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.uid", "").exists())
                                .andExpect(jsonPath("$.uid", "").value(uidDto.getUid()));
        }

        @Test
        void updateUser() throws Exception {
                // GIVEN
                UidDto uidDto = new UidDto(UUID.randomUUID().toString());
                UserDto userDto = mockUserDto();

                ObjectMapper mapper = new ObjectMapper();
                String userJson = mapper.writeValueAsString(userDto);

                // WHEN
                Mockito.when(userPortService.createUser(Mockito.any(), Mockito.any(), Mockito.any()))
                                .thenReturn(uidDto);

                // THEN
                String usersUri = "/api/v1/tenants/" + TENANT_UID + "/organizations/" + ORG_UID + "/users/"
                                + uidDto.getUid();
                mockMvc.perform(post(usersUri)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(userJson)
                                .accept(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8))
                                .andDo(print())
                                .andExpect(status().isNoContent());
        }

        @Test
        void listUsers() throws Exception {
                // GIVEN
                UidDto uidDto = new UidDto(UUID.randomUUID().toString());
                UserDisplayDto userDisplayDto = UserDisplayDto.builder()
                                .email("test.test@test.fr")
                                .firstName("fname")
                                .lastName("lname")
                                .login("tlogin")
                                .middleName("mname")
                                .organization(OrganizationLightDto.builder().kind(OrganizationKind.BU).label("orglabel")
                                                .status(OrganizationStatus.ACTIVE).uid(ORG_UID).build())
                                .uid(uidDto.getUid())
                                .build();
                List<UserDisplayDto> usersList = List.of(userDisplayDto);
                UsersDisplayListDto usersDisplayListDto = new UsersDisplayListDto(usersList);

                // WHEN
                Mockito.when(userPortService.findUsers(Mockito.any(), Mockito.any())).thenReturn(usersDisplayListDto);

                // THEN
                String usersUri = "/api/v1/tenants/" + TENANT_UID + "/organizations/" + ORG_UID + "/users";
                mockMvc.perform(get(usersUri)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8))
                                .andDo(print())
                                .andExpect(status().isOk());
        }

        @Test
        void deleteUser() throws Exception {
                // GIVEN
                UidDto uidDto = new UidDto(UUID.randomUUID().toString());

                // WHEN
                Mockito.when(userPortService.deleteUser(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(1);

                // THEN
                String usersUri = "/api/v1/tenants/" + TENANT_UID + "/organizations/" + ORG_UID + "/users/"
                                + uidDto.getUid();
                mockMvc.perform(delete(usersUri)
                                .characterEncoding(StandardCharsets.UTF_8))
                                .andDo(print())
                                .andExpect(status().isNoContent());
        }

        /**
         * Mock user dto.
         * 
         * @return User DTO
         */
        private UserDto mockUserDto() {
                UserCommonsDto userCommonsDto = UserCommonsDto.builder()
                                .firstName("fname")
                                .lastName("lname")
                                .middleName("mname")
                                .build();
                UserCredentialsDto userCredentialsDto = UserCredentialsDto.builder()
                                .email("email")
                                .login("login")
                                .build();
                UserDto userDto = UserDto.builder()
                                .commons(userCommonsDto)
                                .credentials(userCredentialsDto)
                                .build();
                return userDto;
        }

}
