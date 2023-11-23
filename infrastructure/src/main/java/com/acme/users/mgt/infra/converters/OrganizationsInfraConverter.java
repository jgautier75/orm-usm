package com.acme.users.mgt.infra.converters;

import org.springframework.stereotype.Component;

import com.acme.jga.users.mgt.domain.organizations.v1.Organization;
import com.acme.jga.users.mgt.domain.organizations.v1.OrganizationCommons;
import com.acme.users.mgt.infra.dto.organizations.v1.OrganizationDb;

@Component
public class OrganizationsInfraConverter {

    public OrganizationDb convertOrganizationToOrganizationDb(Organization organization) {
        OrganizationDb organizationDb = null;
        if (organization != null) {
            organizationDb = new OrganizationDb();
            if (organization.getCommons() != null) {
                organizationDb.setCode(organization.getCommons().getCode());
                organizationDb.setCountry(organization.getCommons().getCountry());
                organizationDb.setId(organization.getId());
                organizationDb.setKind(organization.getCommons().getKind());
                organizationDb.setLabel(organization.getCommons().getLabel());
                organizationDb.setStatus(organization.getCommons().getStatus());
                organizationDb.setTenantId(organization.getTenantId());
                organizationDb.setUid(organization.getUid());
            }
        }
        return organizationDb;
    }

    public Organization convertOrganizationDbToOrganization(OrganizationDb orgDb) {
        Organization org = null;
        if (orgDb != null) {
            org = new Organization();
            org.setTenantId(orgDb.getTenantId());
            org.setUid(orgDb.getUid());
            org.setId(orgDb.getId());
            OrganizationCommons commons = OrganizationCommons.builder()
                    .code(orgDb.getCode())
                    .country(orgDb.getCountry())
                    .kind(orgDb.getKind())
                    .label(orgDb.getLabel())
                    .status(orgDb.getStatus())
                    .build();
            org.setCommons(commons);
        }
        return org;
    }

}
