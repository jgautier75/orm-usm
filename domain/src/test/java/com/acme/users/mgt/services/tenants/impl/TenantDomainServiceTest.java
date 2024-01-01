package com.acme.users.mgt.services.tenants.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.dto.tenant.Tenant;
import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.users.mgt.infra.services.impl.events.EventsInfraService;
import com.acme.users.mgt.infra.services.impl.tenants.TenantInfraService;
import com.acme.users.mgt.logging.services.impl.LogService;

@RunWith(MockitoJUnitRunner.class)
public class TenantDomainServiceTest {
    @Mock
    LogService logService;
    @Mock
    MessageSource messageSource;
    @Mock
    TenantInfraService tenantInfraService;
    @Mock
    EventsInfraService eventsInfraService;
    @InjectMocks
    TenantDomainService tenantDomainService;

    @Test
    public void createTenantNominal() throws FunctionalException {
        // GIVEN
        Tenant tenant = mockTenant();
        CompositeId cid = CompositeId.builder().id(1L).uid(UUID.randomUUID().toString()).build();

        // WHEN
        Mockito.when(tenantInfraService.tenantExistsByCode(Mockito.anyString())).thenReturn(false);
        Mockito.when(tenantInfraService.createTenant(Mockito.any())).thenReturn(cid);
        Mockito.doNothing().when(logService).infoS(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doNothing().when(logService).debugS(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.when(eventsInfraService.createEvent(Mockito.any())).thenReturn(UUID.randomUUID().toString());

        // THEN
        CompositeId compositeId = tenantDomainService.createTenant(tenant);
        assertNotNull("Composite id not null", compositeId);
    }

    @Test
    public void createTenantCodeExists() throws FunctionalException {
        // GIVEN
        Tenant tenant = mockTenant();

        // WHEN
        Mockito.when(tenantInfraService.tenantExistsByCode(Mockito.anyString())).thenReturn(true);
        Mockito.when(messageSource.getMessage(Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenReturn("tenant_code_already_used");

        // THEN
        assertThrows(FunctionalException.class, () -> tenantDomainService.createTenant(tenant));
    }

    @Test
    public void updateTenantNominal() throws FunctionalException {
        // GIVEN
        Tenant tenant = mockTenant();

        // WHEN
        Mockito.doNothing().when(logService).infoS(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doNothing().when(logService).debugS(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.when(tenantInfraService.findTenantByUid(Mockito.any())).thenReturn(tenant);
        Mockito.when(tenantInfraService.updateTenant(Mockito.any())).thenReturn(1);
        Mockito.when(eventsInfraService.createEvent(Mockito.any())).thenReturn(UUID.randomUUID().toString());

        // THEN
        Integer nbUpdated = tenantDomainService.updateTenant(tenant);
        assertEquals(1L, nbUpdated.longValue());
    }

    @Test
    public void deleteTenant() throws FunctionalException {
        // GIVEN
        Tenant tenant = mockTenant();

        // WHEN
        Mockito.when(tenantInfraService.findTenantByUid(Mockito.any())).thenReturn(tenant);
        Mockito.doNothing().when(logService).infoS(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doNothing().when(logService).debugS(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.when(tenantInfraService.deleteUsersByTenantId(Mockito.any())).thenReturn(1);
        Mockito.when(tenantInfraService.deleteSectorsByTenantId(Mockito.any())).thenReturn(1);
        Mockito.when(tenantInfraService.deleteOrganizationsByTenantId(Mockito.any())).thenReturn(1);
        Mockito.when(tenantInfraService.deleteTenant(Mockito.any())).thenReturn(1);
        Mockito.when(eventsInfraService.createEvent(Mockito.any())).thenReturn(UUID.randomUUID().toString());

        // THEN
        Integer nbDeleted = tenantDomainService.deleteTenant(UUID.randomUUID().toString());
        assertEquals(1L, nbDeleted.longValue());
    }

    /**
     * Mock tenant.
     * 
     * @return Tenant
     */
    private Tenant mockTenant() {
        return Tenant.builder()
                .code("code")
                .id(1L)
                .label("label")
                .uid(UUID.randomUUID().toString())
                .build();
    }

}
