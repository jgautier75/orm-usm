package com.acme.users.mgt.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.acme.jga.users.mgt.dto.organizations.OrganizationKind;
import com.acme.jga.users.mgt.dto.organizations.OrganizationStatus;
import com.acme.users.mgt.config.AppDebuggingConfig;
import com.acme.users.mgt.config.AppGenericConfig;
import com.acme.users.mgt.dto.port.organizations.v1.OrganizationCommonsDto;
import com.acme.users.mgt.dto.port.organizations.v1.OrganizationDto;
import com.acme.users.mgt.dto.port.shared.UidDto;
import com.acme.users.mgt.logging.services.api.ILogService;
import com.acme.users.mgt.services.api.organization.IOrganizationPortService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = OrganizationsController.class)
class OrganizationsControllerTest {
        private static final String TENANT_UID = UUID.randomUUID().toString();
        @MockBean
        private IOrganizationPortService organizationPortService;
        @MockBean
        private ILogService logService;
        @MockBean
        private AppGenericConfig appGenericConfig;
        @MockBean
        private AppDebuggingConfig appDebuggingConfig;
        @Autowired
        private MockMvc mockMvc;

        @Test
        void createOrganization() throws Exception {
                // GIVEN

                UidDto uidDto = new UidDto(UUID.randomUUID().toString());
                OrganizationCommonsDto orgCommonsDto = OrganizationCommonsDto.builder()
                                .code("org-code")
                                .country("fr")
                                .kind(OrganizationKind.BU)
                                .label("org-label")
                                .status(OrganizationStatus.ACTIVE)
                                .build();

                OrganizationDto organizationDto = OrganizationDto.builder()
                                .commons(orgCommonsDto)
                                .tenantUid(TENANT_UID)
                                .build();
                // mockMvc = MockMvcBuilders.standaloneSetup(organizationsController).build();
                ObjectMapper objectMapper = new ObjectMapper();
                String orgJson = objectMapper.writeValueAsString(organizationDto);

                // WHEN
                Mockito.when(organizationPortService.createOrganization(Mockito.any(), Mockito.any()))
                                .thenReturn(uidDto);
                // THEN
                mockMvc.perform(post("/api/v1/tenants/" + TENANT_UID + "/organizations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(orgJson)
                                .accept(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.uid", "").exists())
                                .andExpect(jsonPath("$.uid", "").value(uidDto.getUid()));
        }

}
