package com.acme.users.mgt.services.sectors.impl;

import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.acme.jga.users.mgt.domain.organizations.v1.Organization;
import com.acme.jga.users.mgt.domain.sectors.v1.Sector;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.dto.tenant.Tenant;
import com.acme.jga.users.mgt.exceptions.FunctionalErrorsTypes;
import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.users.mgt.infra.services.api.sectors.ISectorsInfraService;
import com.acme.users.mgt.services.organizations.api.IOrganizationsDomainService;
import com.acme.users.mgt.services.sectors.api.ISectorsDomainService;
import com.acme.users.mgt.services.tenants.api.ITenantDomainService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SectorsDomainService implements ISectorsDomainService {
    private final ITenantDomainService tenantDomainService;
    private final IOrganizationsDomainService organizationsDomainService;
    private final ISectorsInfraService sectorsInfraService;
    private final MessageSource messageSource;

    @Transactional(rollbackFor = { FunctionalException.class })
    @Override
    public CompositeId createSector(String tenantUid, String organizationUid, Sector sector)
            throws FunctionalException {

        // Find tenant
        Tenant tenant = tenantDomainService.findTenantByUid(tenantUid);

        // Find organization
        Organization organization = organizationsDomainService.findOrganizationByTenantAndUid(tenant.getId(),
                organizationUid);

        Optional<Long> optSectorId = sectorsInfraService.existsByCode(sector.getCode());
        if (optSectorId.isPresent()) {
            throw new FunctionalException(FunctionalErrorsTypes.SECTOR_CODE_ALREADY_USED.name(), null,
                    messageSource.getMessage("sector_code_already_used",
                            new Object[] { sector.getCode() }, LocaleContextHolder.getLocale()));
        }

        // Ensure parent sector exists
        if (!ObjectUtils.isEmpty(sector.getParentUid())) {
            Sector parentSector = findSectorByUidTenantOrg(tenantUid, organizationUid, sector.getParentUid());
            sector.setParentId(parentSector.getId());
        }

        return sectorsInfraService.createSector(tenant.getId(), organization.getId(), sector);
    }

    @Override
    public Sector findSectorByUidTenantOrg(String tenantUid, String organizationUid, String sectorUid)
            throws FunctionalException {
        // Find tenant
        Tenant tenant = tenantDomainService.findTenantByUid(tenantUid);

        // Find organization
        Organization organization = organizationsDomainService.findOrganizationByTenantAndUid(tenant.getId(),
                organizationUid);

        return findSectorByUidTenantOrg(tenant.getId(), organization.getId(), sectorUid);
    }

    @Override
    public Sector findSectorByUidTenantOrg(Long tenantId, Long organizationId, String sectorUid)
            throws FunctionalException {
        Sector sector = sectorsInfraService.findSectorByUid(tenantId, organizationId, sectorUid);
        if (sector == null) {
            throw new FunctionalException(FunctionalErrorsTypes.SECTOR_NOT_FOUND.name(), null, messageSource
                    .getMessage("sector_not_found", new Object[] { sectorUid }, LocaleContextHolder.getLocale()));
        }
        return sector;
    }

    @Override
    public Sector fetchSectorsWithHierarchy(String tenantUid, String organizationUid) throws FunctionalException {
        // Find tenant
        Tenant tenant = tenantDomainService.findTenantByUid(tenantUid);

        // Find organization
        Organization organization = organizationsDomainService.findOrganizationByTenantAndUid(tenant.getId(),
                organizationUid);

        return sectorsInfraService.fetchSectorsWithHierarchy(tenant.getId(), organization.getId());
    }

}
