package com.acme.users.mgt.services.organizations.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.integration.channel.PublishSubscribeChannel;

import com.acme.jga.users.mgt.domain.organizations.v1.Organization;
import com.acme.jga.users.mgt.domain.organizations.v1.OrganizationCommons;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.dto.organizations.OrganizationKind;
import com.acme.jga.users.mgt.dto.organizations.OrganizationStatus;
import com.acme.jga.users.mgt.dto.tenant.Tenant;
import com.acme.jga.users.mgt.exceptions.FunctionalErrorsTypes;
import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.users.mgt.events.EventBuilderOrganization;
import com.acme.users.mgt.infra.services.impl.events.EventsInfraService;
import com.acme.users.mgt.infra.services.impl.organizations.OrganizationsInfraService;
import com.acme.users.mgt.infra.services.impl.sectors.SectorsInfraService;
import com.acme.users.mgt.logging.services.impl.LogService;
import com.acme.users.mgt.services.tenants.impl.TenantDomainService;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationsDomainServiceTest {
        @RegisterExtension
        static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();
        @Mock
        OrganizationsInfraService organizationsInfraService;
        @Mock
        TenantDomainService tenantDomainService;
        @Mock
        MessageSource messageSource;
        @Mock
        LogService logService;
        @Mock
        SectorsInfraService sectorsInfraService;
        @Mock
        EventsInfraService eventsInfraService;
        @Mock
        PublishSubscribeChannel eventAuditChannel; 
        @Mock
        EventBuilderOrganization builderOrganization;
        @InjectMocks
        OrganizationsDomainService organizationsDomainService;

        @Before
        public void init(){
                organizationsDomainService.setSdkTracerProvider(otelTesting.getOpenTelemetry().getTracerProvider());
        }

        @Test
        public void createOrganizationNominal() throws FunctionalException {              
                // GIVEN
                Tenant tenant = mockTenant();
                CompositeId compositeId = CompositeId.builder().id(1L).uid(UUID.randomUUID().toString()).build();
                OrganizationCommons organizationCommons = OrganizationCommons.builder().code("ren").country("fr")
                                .kind(OrganizationKind.COMMUNITY).label("Rennes").status(OrganizationStatus.ACTIVE)
                                .build();
                Organization organization = Organization.builder().commons(organizationCommons).tenantId(tenant.getId())
                                .build();                

                // WHEN
                Mockito.when(tenantDomainService.findTenantByUid(Mockito.any())).thenReturn(tenant);
                Mockito.when(organizationsInfraService.codeAlreadyUsed(Mockito.any())).thenReturn(Optional.empty());
                Mockito.when(organizationsInfraService.createOrganization(Mockito.any())).thenReturn(compositeId);
                Mockito.when(eventsInfraService.createEvent(Mockito.any())).thenReturn(UUID.randomUUID().toString());
                Mockito.when(sectorsInfraService.createSector(Mockito.any(), Mockito.any(), Mockito.any()))
                                .thenReturn(compositeId);
                Mockito.when(eventsInfraService.createEvent(Mockito.any())).thenReturn(UUID.randomUUID().toString());
                Span rootSpan = otelTesting.getOpenTelemetry().getTracer("test").spanBuilder("test").startSpan();
                // THEN
                CompositeId orgCompositeId = organizationsDomainService.createOrganization(tenant.getUid(),
                                organization, rootSpan);
                assertNotNull("Organization not null", orgCompositeId);
        }

        @Test
        public void createOrganizationNoTenant() throws FunctionalException {
                // GIVEN
                Organization organization = mockOrganization();
                Span rootSpan = otelTesting.getOpenTelemetry().getTracer("test").spanBuilder("test").startSpan();

                // WHEN
                Mockito.when(tenantDomainService.findTenantByUid(Mockito.any())).thenThrow(new FunctionalException(
                                FunctionalErrorsTypes.TENANT_NOT_FOUND.name(), null,
                                FunctionalErrorsTypes.TENANT_NOT_FOUND.name()));

                // THEN
                assertThrows(FunctionalException.class,
                                () -> organizationsDomainService.createOrganization(UUID.randomUUID().toString(),
                                                organization, rootSpan));

        }

        @Test
        public void updateOrganizationNominal() throws FunctionalException {
                // GIVEN
                Tenant tenant = mockTenant();
                Organization organization = mockOrganization();

                // WHEN
                Mockito.when(tenantDomainService.findTenantByUid(Mockito.any())).thenReturn(tenant);
                Mockito.when(organizationsInfraService.findOrganizationByUid(Mockito.any(), Mockito.any()))
                                .thenReturn(organization);
                //Mockito.when(eventsInfraService.createEvent(Mockito.any())).thenReturn(UUID.randomUUID().toString());

                // THEN
                Integer nbUpdated = organizationsDomainService.updateOrganization(tenant.getUid(),
                                organization.getUid(),
                                organization);
                assertEquals(Integer.valueOf(0), nbUpdated);
        }

        @Test
        public void updateOrganizationNoTenant() throws FunctionalException {
                // GIVEN
                Tenant tenant = mockTenant();
                Organization organization = mockOrganization();

                // WHEN
                Mockito.when(tenantDomainService.findTenantByUid(Mockito.any())).thenThrow(new FunctionalException(
                                FunctionalErrorsTypes.TENANT_NOT_FOUND.name(), null,
                                FunctionalErrorsTypes.TENANT_NOT_FOUND.name()));
                // THEN
                assertThrows(FunctionalException.class,
                                () -> organizationsDomainService.updateOrganization(tenant.getUid(),
                                                organization.getUid(),
                                                organization));
        }

        @Test
        public void deleteNominal() throws FunctionalException {
                // GIVEN
                Tenant tenant = mockTenant();
                Organization organization = mockOrganization();

                // WHEN
                Mockito.when(tenantDomainService.findTenantByUid(Mockito.any())).thenReturn(tenant);
                Mockito.when(organizationsInfraService.findOrganizationByUid(Mockito.any(), Mockito.any()))
                                .thenReturn(organization);
                Mockito.when(organizationsInfraService.deleteUsersByOrganization(Mockito.any(), Mockito.any()))
                                .thenReturn(1);
                Mockito.when(organizationsInfraService.deleteSectors(Mockito.any(), Mockito.any())).thenReturn(1);
                Mockito.when(organizationsInfraService.deleteById(Mockito.any(), Mockito.any())).thenReturn(1);
                Mockito.when(eventsInfraService.createEvent(Mockito.any())).thenReturn(UUID.randomUUID().toString());

                // THEN
                Integer nbDeleted = organizationsDomainService.deleteOrganization(tenant.getUid(),
                                organization.getUid());
                assertNotNull("NbDelete", nbDeleted);

        }

        private Tenant mockTenant() {
                return Tenant.builder()
                                .code("tenant")
                                .id(1L)
                                .label("label")
                                .uid(UUID.randomUUID().toString())
                                .build();
        }

        private Organization mockOrganization() {

                Tenant tenant = mockTenant();

                OrganizationCommons organizationCommons = OrganizationCommons.builder().code("ren").country("fr")
                                .kind(OrganizationKind.COMMUNITY).label("Rennes").status(OrganizationStatus.ACTIVE)
                                .build();
                Organization organization = Organization.builder().commons(organizationCommons).tenantId(tenant.getId())
                                .build();
                return organization;
        }

}
