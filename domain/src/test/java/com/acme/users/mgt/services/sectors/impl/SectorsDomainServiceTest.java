package com.acme.users.mgt.services.sectors.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import com.acme.jga.users.mgt.domain.organizations.v1.Organization;
import com.acme.jga.users.mgt.domain.organizations.v1.OrganizationCommons;
import com.acme.jga.users.mgt.domain.sectors.v1.Sector;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.dto.organizations.OrganizationKind;
import com.acme.jga.users.mgt.dto.organizations.OrganizationStatus;
import com.acme.jga.users.mgt.dto.tenant.Tenant;
import com.acme.jga.users.mgt.exceptions.FunctionalErrorsTypes;
import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.users.mgt.infra.services.impl.events.EventsInfraService;
import com.acme.users.mgt.infra.services.impl.sectors.SectorsInfraService;
import com.acme.users.mgt.services.organizations.impl.OrganizationsDomainService;
import com.acme.users.mgt.services.tenants.impl.TenantDomainService;

@RunWith(MockitoJUnitRunner.class)
public class SectorsDomainServiceTest {
        @Mock
        TenantDomainService tenantDomainService;
        @Mock
        OrganizationsDomainService organizationsDomainService;
        @Mock
        SectorsInfraService sectorsInfraService;
        @Mock
        MessageSource messageSource;
        @Mock
        EventsInfraService eventsInfraService;
        @InjectMocks
        SectorsDomainService sectorsDomainService;

        @Test
        public void createSectorNominal() throws FunctionalException {
                // GIVEN
                Tenant tenant = mockTenant();
                Organization organization = mockOrganization(tenant);
                CompositeId compositeId = CompositeId.builder().id(1L).uid(UUID.randomUUID().toString()).build();
                Sector sector = Sector.builder().code("scode").id(1L).label("slabel").orgId(organization.getId())
                                .root(false).tenantId(tenant.getId()).uid(UUID.randomUUID().toString()).build();

                // WHEN
                Mockito.when(tenantDomainService.findTenantByUid(Mockito.any())).thenReturn(tenant);
                Mockito.when(organizationsDomainService.findOrganizationByTenantAndUid(Mockito.any(), Mockito.any()))
                                .thenReturn(organization);
                Mockito.when(sectorsInfraService.existsByCode(Mockito.any())).thenReturn(Optional.empty());
                Mockito.when(sectorsInfraService.createSector(Mockito.any(), Mockito.any(), Mockito.any()))
                                .thenReturn(compositeId);
                Mockito.when(eventsInfraService.createEvent(Mockito.any())).thenReturn(UUID.randomUUID().toString());

                // THEN
                CompositeId sectorCompId = sectorsDomainService.createSector(tenant.getUid(), organization.getUid(),
                                sector);
                assertNotNull(sectorCompId);
        }

        @Test
        public void createSectorTenantNotFound() throws FunctionalException {
                // GIVEN
                Tenant tenant = mockTenant();
                Organization organization = mockOrganization(tenant);
                Sector sector = Sector.builder().code("scode").id(1L).label("slabel").orgId(organization.getId())
                                .root(false).tenantId(tenant.getId()).uid(UUID.randomUUID().toString()).build();

                // WHEN
                Mockito.when(tenantDomainService.findTenantByUid(Mockito.any()))
                                .thenThrow(new FunctionalException(FunctionalErrorsTypes.TENANT_NOT_FOUND.name(), null,
                                                FunctionalErrorsTypes.TENANT_NOT_FOUND.name()));

                // THEN
                assertThrows(FunctionalException.class,
                                () -> sectorsDomainService.createSector(tenant.getUid(), organization.getUid(),
                                                sector));
        }

        @Test
        public void createSectorNoOrganization() throws FunctionalException {
                // GIVEN
                Tenant tenant = mockTenant();
                Organization organization = mockOrganization(tenant);
                Sector sector = Sector.builder().code("scode").id(1L).label("slabel").orgId(organization.getId())
                                .root(false).tenantId(tenant.getId()).uid(UUID.randomUUID().toString()).build();

                // WHEN
                Mockito.when(tenantDomainService.findTenantByUid(Mockito.any())).thenReturn(tenant);
                Mockito.when(organizationsDomainService.findOrganizationByTenantAndUid(Mockito.any(), Mockito.any()))
                                .thenThrow(new FunctionalException(FunctionalErrorsTypes.TENANT_NOT_FOUND.name(), null,
                                                FunctionalErrorsTypes.TENANT_NOT_FOUND.name()));

                // THEN
                assertThrows(FunctionalException.class,
                                () -> sectorsDomainService.createSector(tenant.getUid(), organization.getUid(),
                                                sector));
        }

        private Tenant mockTenant() {
                return Tenant.builder()
                                .code("tcode")
                                .id(1L)
                                .label("tlabel")
                                .uid(UUID.randomUUID().toString()).build();
        }

        private Organization mockOrganization(Tenant tenant) {
                OrganizationCommons organizationCommons = OrganizationCommons.builder().code("ren").country("fr")
                                .kind(OrganizationKind.COMMUNITY).label("Rennes").status(OrganizationStatus.ACTIVE)
                                .build();
                Organization organization = Organization.builder().commons(organizationCommons).tenantId(tenant.getId())
                                .build();
                return organization;
        }

}
