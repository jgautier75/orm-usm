package com.acme.users.mgt.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.acme.jga.users.mgt.dto.organizations.OrganizationKind;
import com.acme.jga.users.mgt.dto.organizations.OrganizationStatus;
import com.acme.users.mgt.config.AppDebuggingConfig;
import com.acme.users.mgt.config.AppGenericConfig;
import com.acme.users.mgt.config.MicrometerPrometheus;
import com.acme.users.mgt.config.OpenTelemetryTestConfig;
import com.acme.users.mgt.dto.port.organizations.v1.OrganizationCommonsDto;
import com.acme.users.mgt.dto.port.organizations.v1.OrganizationDto;
import com.acme.users.mgt.dto.port.shared.UidDto;
import com.acme.users.mgt.logging.services.api.ILogService;
import com.acme.users.mgt.services.api.organization.IOrganizationPortService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = OrganizationsController.class)
@Import(value = {OpenTelemetryTestConfig.class})
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
        @MockBean
        private MicrometerPrometheus micrometerPrometheus;
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
                ObjectMapper objectMapper = new ObjectMapper();
                String orgJson = objectMapper.writeValueAsString(organizationDto);

                // WHEN
                Mockito.when(organizationPortService.createOrganization(Mockito.any(), Mockito.any(), Mockito.any()))
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

        @Test
        void updateOrganization() throws Exception {
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
                ObjectMapper objectMapper = new ObjectMapper();
                String orgJson = objectMapper.writeValueAsString(organizationDto);

                // WHEN
                Mockito.when(organizationPortService.updateOrganization(Mockito.any(), Mockito.any(), Mockito.any()))
                                .thenReturn(1);
                // THEN
                String targetUri = "/api/v1/tenants/" + TENANT_UID + "/organizations/" + uidDto.getUid();
                mockMvc.perform(post(targetUri)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(orgJson)
                                .accept(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8))
                                .andExpect(status().isNoContent());
        }

        @Test
        void deleteOrganization() throws Exception {
                // GIVEN
                UidDto uidDto = new UidDto(UUID.randomUUID().toString());

                // WHEN
                Mockito.when(organizationPortService.deleteOrganization(Mockito.any(), Mockito.any())).thenReturn(1);

                // THEN
                String targetUri = "/api/v1/tenants/" + TENANT_UID + "/organizations/" + uidDto.getUid();
                mockMvc.perform(delete(targetUri)
                                .accept(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8))
                                .andExpect(status().isNoContent());
        }

}
