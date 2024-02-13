package com.acme.users.mgt.infra.services.impl.organizations;

import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.acme.jga.users.mgt.domain.organizations.v1.Organization;
import com.acme.jga.users.mgt.domain.organizations.v1.OrganizationCommons;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.dto.organizations.OrganizationKind;
import com.acme.jga.users.mgt.dto.organizations.OrganizationStatus;
import com.acme.users.mgt.infra.converters.OrganizationsInfraConverter;
import com.acme.users.mgt.infra.dao.api.organizations.IOrganizationsDao;
import com.acme.users.mgt.infra.dto.organizations.v1.OrganizationDb;

import io.opentelemetry.api.trace.Span;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationsInfraServiceTest {
    private static final Long TENANT_ID = 1L;
    @InjectMocks
    private OrganizationsInfraService organizationsInfraService;
    @Mock
    private OrganizationsInfraConverter organizationsInfraConverter;
    @Mock
    private IOrganizationsDao organizationsDao;

    @Test
    public void createOrganization() {
        // GIVEN
        OrganizationCommons organizationCommons = OrganizationCommons.builder()
                .code("test-code")
                .country("fr")
                .kind(OrganizationKind.TENANT)
                .label("org-create")
                .status(OrganizationStatus.ACTIVE)
                .build();
        Organization org = Organization.builder()
                .tenantId(1L)
                .commons(organizationCommons)
                .uid(UUID.randomUUID().toString())
                .build();
        OrganizationDb orgDb = mockOrganizationDb();
        CompositeId compositeId = CompositeId.builder().id(2L).uid(UUID.randomUUID().toString()).build();

        // WHEN
        Mockito.when(organizationsInfraConverter.convertOrganizationToOrganizationDb(Mockito.any())).thenReturn(orgDb);
        Mockito.when(organizationsDao.createOrganization(Mockito.any())).thenReturn(compositeId);

        // THEN
        CompositeId id = organizationsInfraService.createOrganization(org);
        assertNotNull("CompositeId not null", id);
    }

    @Test
    public void findAllOrganizations() {
        // GIVEN
        OrganizationDb orgDb = mockOrganizationDb();
        List<OrganizationDb> organizationDbs = List.of(orgDb);
        Organization org = mockOrganization();

        // WHEN
        Mockito.when(organizationsDao.findAllOrganizations(Mockito.any())).thenReturn(organizationDbs);
        Mockito.when(organizationsInfraConverter.convertOrganizationDbToOrganization(Mockito.any())).thenReturn(org);

        // THEN

        List<Organization> orgs = organizationsInfraService.findAllOrganizations(TENANT_ID, Span.current());
        assertNotNull("Organizations list", orgs);
    }

    /**
     * Mock organization.
     * 
     * @return Organization
     */
    private Organization mockOrganization() {
        OrganizationCommons organizationCommons = OrganizationCommons.builder()
                .code("test-code")
                .country("fr")
                .kind(OrganizationKind.TENANT)
                .label("org-create")
                .status(OrganizationStatus.ACTIVE)
                .build();
        Organization org = Organization.builder()
                .tenantId(TENANT_ID)
                .commons(organizationCommons)
                .uid(UUID.randomUUID().toString())
                .build();
        return org;
    }

    /**
     * Mock organization Db.
     * 
     * @return Organization db
     */
    private OrganizationDb mockOrganizationDb() {
        return OrganizationDb.builder()
                .code("test-code")
                .country("fr")
                .kind(OrganizationKind.TENANT)
                .label("org-label")
                .status(OrganizationStatus.ACTIVE)
                .tenantId(TENANT_ID)
                .build();
    }

}
