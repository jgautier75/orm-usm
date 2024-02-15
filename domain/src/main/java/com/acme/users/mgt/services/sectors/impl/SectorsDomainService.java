package com.acme.users.mgt.services.sectors.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.acme.jga.users.mgt.domain.events.v1.AuditAction;
import com.acme.jga.users.mgt.domain.events.v1.AuditChange;
import com.acme.jga.users.mgt.domain.events.v1.AuditEvent;
import com.acme.jga.users.mgt.domain.events.v1.AuditScope;
import com.acme.jga.users.mgt.domain.events.v1.EventStatus;
import com.acme.jga.users.mgt.domain.events.v1.EventTarget;
import com.acme.jga.users.mgt.domain.organizations.v1.Organization;
import com.acme.jga.users.mgt.domain.sectors.v1.Sector;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.dto.tenant.Tenant;
import com.acme.jga.users.mgt.exceptions.FunctionalErrorsTypes;
import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.jga.users.mgt.utils.DateTimeUtils;
import com.acme.users.mgt.config.KafkaConfig;
import com.acme.users.mgt.events.EventBuilderSector;
import com.acme.users.mgt.infra.services.api.events.IEventsInfraService;
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
        private final IEventsInfraService eventsInfraService;
        private final PublishSubscribeChannel eventAuditChannel;
        private final EventBuilderSector eventBuilderSector;

        @Transactional(rollbackFor = { FunctionalException.class })
        @Override
        public CompositeId createSector(String tenantUid, String organizationUid, Sector sector)
                        throws FunctionalException {

                // Find tenant
                Tenant tenant = tenantDomainService.findTenantByUid(tenantUid);

                // Find organization
                Organization organization = organizationsDomainService.findOrganizationByTenantAndUid(tenant.getId(),
                                organizationUid, false);

                Optional<Long> optSectorId = sectorsInfraService.existsByCode(sector.getCode());
                if (optSectorId.isPresent()) {
                        throw new FunctionalException(FunctionalErrorsTypes.SECTOR_CODE_ALREADY_USED.name(), null,
                                        messageSource.getMessage("sector_code_already_used",
                                                        new Object[] { sector.getCode() },
                                                        LocaleContextHolder.getLocale()));
                }

                // Ensure parent sector exists
                if (!ObjectUtils.isEmpty(sector.getParentUid())) {
                        Sector parentSector = findSectorByUidTenantOrg(tenantUid, organizationUid,
                                        sector.getParentUid());
                        sector.setParentId(parentSector.getId());
                }

                CompositeId sectorCompositeId = sectorsInfraService.createSector(tenant.getId(), organization.getId(),
                                sector);

                // Create sector audit event
                AuditEvent sectorAuditEvent = AuditEvent.builder()
                                .action(AuditAction.CREATE)
                                .objectUid(sectorCompositeId.getUid())
                                .target(EventTarget.SECTOR)
                                .scope(AuditScope.builder().tenantName(tenant.getLabel()).tenantUid(tenantUid)
                                                .organizationUid(organization.getUid())
                                                .organizationName(organization.getCommons().getLabel())
                                                .build())
                                .status(EventStatus.PENDING)
                                .createdAt(DateTimeUtils.nowIso())
                                .lastUpdatedAt(DateTimeUtils.nowIso())
                                .build();
                eventsInfraService.createEvent(sectorAuditEvent);
                eventAuditChannel.send(MessageBuilder.withPayload(KafkaConfig.AUDIT_WAKE_UP).build());

                return sectorCompositeId;
        }

        @Override
        public Sector findSectorByUidTenantOrg(String tenantUid, String organizationUid, String sectorUid)
                        throws FunctionalException {
                // Find tenant
                Tenant tenant = tenantDomainService.findTenantByUid(tenantUid);

                // Find organization
                Organization organization = organizationsDomainService.findOrganizationByTenantAndUid(tenant.getId(),
                                organizationUid, false);

                return findSectorByUidTenantOrg(tenant.getId(), organization.getId(), sectorUid);
        }

        @Override
        public Sector findSectorByUidTenantOrg(Long tenantId, Long organizationId, String sectorUid)
                        throws FunctionalException {
                Sector sector = sectorsInfraService.findSectorByUid(tenantId, organizationId, sectorUid);
                if (sector == null) {
                        throw new FunctionalException(FunctionalErrorsTypes.SECTOR_NOT_FOUND.name(), null, messageSource
                                        .getMessage("sector_not_found", new Object[] { sectorUid },
                                                        LocaleContextHolder.getLocale()));
                }
                return sector;
        }

        @Override
        public Sector fetchSectorsWithHierarchy(String tenantUid, String organizationUid) throws FunctionalException {
                // Find tenant
                Tenant tenant = tenantDomainService.findTenantByUid(tenantUid);

                // Find organization
                Organization organization = organizationsDomainService.findOrganizationByTenantAndUid(tenant.getId(),
                                organizationUid, false);

                return sectorsInfraService.fetchSectorsWithHierarchy(tenant.getId(), organization.getId());
        }

        @Transactional(rollbackFor = { FunctionalException.class })
        @Override
        public Integer updateSector(String tenantUid, String organizationUid, String sectorUid, Sector sector)
                        throws FunctionalException {
                // Find tenant
                Tenant tenant = tenantDomainService.findTenantByUid(tenantUid);

                // Find organization
                Organization organization = organizationsDomainService.findOrganizationByTenantAndUid(tenant.getId(),
                                organizationUid, false);

                // Find sector
                Sector rdbmsSector = findSectorByUidTenantOrg(tenant.getId(), organization.getId(), sectorUid);
                sector.setId(rdbmsSector.getId());
                sector.setUid(rdbmsSector.getUid());
                sector.setTenantId(rdbmsSector.getTenantId());
                sector.setOrgId(rdbmsSector.getOrgId());

                if (sector.getParentUid() != null) {
                        Sector parentSector = findSectorByUidTenantOrg(tenant.getId(), organization.getId(),
                                        sector.getParentUid());
                        sector.setParentId(parentSector.getId());
                }

                List<AuditChange> auditChanges = eventBuilderSector.buildAuditsChange(rdbmsSector, sector);
                boolean hasChanges = !CollectionUtils.isEmpty(auditChanges);

                if (hasChanges) {
                        // Create sector audit event
                        AuditEvent sectorAuditEvent = AuditEvent.builder()
                                        .action(AuditAction.UPDATE)
                                        .objectUid(rdbmsSector.getUid())
                                        .target(EventTarget.SECTOR)
                                        .changes(auditChanges)
                                        .scope(AuditScope.builder().tenantName(tenant.getLabel()).tenantUid(tenantUid)
                                                        .organizationUid(organization.getUid())
                                                        .organizationName(organization.getCommons().getLabel())
                                                        .build())
                                        .status(EventStatus.PENDING)
                                        .createdAt(DateTimeUtils.nowIso())
                                        .lastUpdatedAt(DateTimeUtils.nowIso())
                                        .build();
                        eventsInfraService.createEvent(sectorAuditEvent);
                        return sectorsInfraService.updateSector(tenant.getId(), organization.getId(), sector);
                } else {
                        return 0;
                }
        }

}
