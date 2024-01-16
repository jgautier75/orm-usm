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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.acme.users.mgt.config.AppDebuggingConfig;
import com.acme.users.mgt.config.AppGenericConfig;
import com.acme.users.mgt.config.MicrometerPrometheus;
import com.acme.users.mgt.dto.port.shared.UidDto;
import com.acme.users.mgt.dto.port.tenants.v1.TenantDto;
import com.acme.users.mgt.logging.services.api.ILogService;
import com.acme.users.mgt.services.api.tenant.ITenantPortService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = TenantsController.class)
class TenantsControllerTest {
    @MockBean
    private ITenantPortService tenantPortService;
    @MockBean
    private ILogService logService;
    @MockBean
    private MicrometerPrometheus micrometerPrometheus;
    @MockBean
    private AppGenericConfig appGenericConfig;
    @MockBean
    private AppDebuggingConfig appDebuggingConfig;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void createTenant() throws Exception {
        // GIVEN
        UidDto uidDto = new UidDto(UUID.randomUUID().toString());
        TenantDto tenantDto = new TenantDto("tenant-code", "tenant-label");
        ObjectMapper mapper = new ObjectMapper();
        String tenantJson = mapper.writeValueAsString(tenantDto);

        // WHEN
        Mockito.when(tenantPortService.createTenant(Mockito.any())).thenReturn(uidDto);

        // THEN
        mockMvc.perform(post("/api/v1/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tenantJson)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uid", "").exists())
                .andExpect(jsonPath("$.uid", "").value(uidDto.getUid()));
    }

    @Test
    void updateTenant() throws Exception {
        // GIVEN
        UidDto uidDto = new UidDto(UUID.randomUUID().toString());
        TenantDto tenantDto = new TenantDto("tenant-code", "tenant-label");
        ObjectMapper mapper = new ObjectMapper();
        String tenantJson = mapper.writeValueAsString(tenantDto);

        // WHEN
        Mockito.when(tenantPortService.updateTenant(Mockito.any(), Mockito.any())).thenReturn(1);

        // THEN
        String targetUri = "/api/v1/tenants/" + uidDto.getUid();
        mockMvc.perform(post(targetUri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(tenantJson)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTenant() throws Exception {
        // GIVEN
        UidDto uidDto = new UidDto(UUID.randomUUID().toString());

        // WHEN
        Mockito.when(tenantPortService.deleteTenant(Mockito.any())).thenReturn(1);

        // THEN
        String targetUri = "/api/v1/tenants/" + uidDto.getUid();
        mockMvc.perform(delete(targetUri)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNoContent());
    }

}
