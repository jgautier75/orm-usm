package com.acme.users.mgt.services.organizations.impl;

import java.util.List;
import java.util.Optional;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.jga.users.mgt.domain.organizations.v1.Organization;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.dto.tenant.Tenant;
import com.acme.jga.users.mgt.exceptions.FunctionalErrorsTypes;
import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.users.mgt.infra.services.api.organizations.IOrganizationsInfraService;
import com.acme.users.mgt.logging.services.api.ILogService;
import com.acme.users.mgt.services.organizations.api.IOrganizationsDomainService;
import com.acme.users.mgt.services.tenants.api.ITenantDomainService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationsDomainService implements IOrganizationsDomainService {
    private final IOrganizationsInfraService organizationsInfraService;
    private final ITenantDomainService tenantDomainService;
    private final MessageSource messageSource;
    private final ILogService logService;

    @Override
    public CompositeId createOrganization(String tenantUid, Organization organization) throws FunctionalException {
        // Find tenant
        Tenant tenant = tenantDomainService.findTenantByUid(tenantUid);

        Optional<Long> orgCodeUsed = organizationsInfraService.codeAlreadyUsed(organization.getCommons().getCode());
        if (orgCodeUsed.isPresent()) {
            throw new FunctionalException(FunctionalErrorsTypes.ORG_CODE_ALREADY_USED.name(), null,
                    messageSource.getMessage("org_code_already_used",
                            new Object[] { organization.getCommons().getCode() }, LocaleContextHolder.getLocale()));
        }

        organization.setTenantId(tenant.getId());
        return organizationsInfraService.createOrganization(organization);
    }

    @Override
    public List<Organization> findAllOrganizations(Long tenantId) {
        return organizationsInfraService.findAllOrganizations(tenantId);
    }

    @Override
    public Organization findOrganizationByTenantAndUid(Long tenantId, String orgUid) throws FunctionalException {
        Organization org = organizationsInfraService.findOrganizationByUid(tenantId, orgUid);
        if (org == null) {
            throw new FunctionalException(FunctionalErrorsTypes.ORG_NOT_FOUND.name(), null,
                    messageSource.getMessage("org_not_found_by_uid", new Object[] { orgUid },
                            LocaleContextHolder.getLocale()));

        }
        return org;
    }

    @Override
    public void updateOrganization(String tenantUid, String orgUid, Organization organization)
            throws FunctionalException {

        String callerName = this.getClass().getName() + "-updateOrganization";

        logService.infoS(callerName, "Update organization [%s] of tenant [%s]", new Object[] { tenantUid, orgUid });

        // Find tenant
        Tenant tenant = tenantDomainService.findTenantByUid(tenantUid);

        // Find organization by tenant and uid
        Organization org = organizationsInfraService.findOrganizationByUid(tenant.getId(), orgUid);

        organization.setId(org.getId());
        organization.setTenantId(tenant.getId());
        organization.setUid(orgUid);

        organizationsInfraService.updateOrganization(tenant.getId(), organization.getId(),
                organization.getCommons().getCode(),
                organization.getCommons().getLabel(), organization.getCommons().getCountry(),
                organization.getCommons().getStatus());

    }

    @Override
    public List<Organization> findOrgsByIdList(List<Long> orgIds) {
        return organizationsInfraService.findOrgsByIdList(orgIds);
    }

    @Transactional
    @Override
    public void deleteOrganization(String tenantUid, String orgUid) throws FunctionalException {
        String callerName = this.getClass().getName() + "-deleteOrganization";

        // Find tenant
        Tenant tenant = tenantDomainService.findTenantByUid(tenantUid);

        // Find organization
        Organization organization = organizationsInfraService.findOrganizationByUid(tenant.getId(), orgUid);

        // Delete users
        Integer nbUsersDeleted = organizationsInfraService.deleteUsersByOrganization(tenant.getId(),
                organization.getId());
        logService.debugS(callerName, "Nb of users deleted: [%s]", new Object[] { nbUsersDeleted });

        Integer nbOrgDeleted = organizationsInfraService.deleteById(tenant.getId(), organization.getId());
        logService.debugS(callerName, "Nb of organizations deleted: [%s]", new Object[] { nbOrgDeleted });
    }

}
