package com.acme.users.mgt.services.organizations.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.jga.users.mgt.domain.events.v1.AuditAction;
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
import com.acme.users.mgt.infra.services.api.events.IEventsInfraService;
import com.acme.users.mgt.infra.services.api.organizations.IOrganizationsInfraService;
import com.acme.users.mgt.infra.services.api.sectors.ISectorsInfraService;
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
        private final ISectorsInfraService sectorsInfraService;
        private final IEventsInfraService eventsInfraService;
        private final PublishSubscribeChannel eventAuditChannel;

        @Transactional
        @Override
        public CompositeId createOrganization(String tenantUid, Organization organization) throws FunctionalException {
                // Find tenant
                Tenant tenant = tenantDomainService.findTenantByUid(tenantUid);

                Optional<Long> orgCodeUsed = organizationsInfraService
                                .codeAlreadyUsed(organization.getCommons().getCode());
                if (orgCodeUsed.isPresent()) {
                        throw new FunctionalException(FunctionalErrorsTypes.ORG_CODE_ALREADY_USED.name(), null,
                                        messageSource.getMessage("org_code_already_used",
                                                        new Object[] { organization.getCommons().getCode() },
                                                        LocaleContextHolder.getLocale()));
                }

                organization.setTenantId(tenant.getId());
                CompositeId orgCompositeId = organizationsInfraService.createOrganization(organization);

                // Create organization audit event
                AuditEvent orgAuditEvent = AuditEvent.builder()
                                .action(AuditAction.CREATE)
                                .objectUid(orgCompositeId.getUid())
                                .infos(organization.getCommons().getLabel())
                                .target(EventTarget.ORGANIZATION)
                                .scope(AuditScope.builder().tenantName(tenant.getLabel()).tenantUid(tenantUid)
                                                .organizationUid(orgCompositeId.getUid())
                                                .organizationName(organization.getCommons().getLabel())
                                                .build())
                                .status(EventStatus.PENDING)
                                .createdAt(DateTimeUtils.nowIso())
                                .lastUpdatedAt(DateTimeUtils.nowIso())
                                .build();
                eventsInfraService.createEvent(orgAuditEvent);

                // Create root sector
                Sector sector = Sector.builder()
                                .code(organization.getCommons().getCode())
                                .label(organization.getCommons().getLabel())
                                .orgId(orgCompositeId.getId())
                                .root(true)
                                .tenantId(tenant.getId())
                                .build();
                CompositeId sectorCompositeId = sectorsInfraService.createSector(tenant.getId(), orgCompositeId.getId(),
                                sector);

                // Create sector audit event
                AuditEvent sectorAuditEvent = AuditEvent.builder()
                                .action(AuditAction.CREATE)
                                .objectUid(sectorCompositeId.getUid())
                                .infos(organization.getCommons().getLabel())
                                .target(EventTarget.SECTOR)
                                .scope(AuditScope.builder().tenantName(tenant.getLabel()).tenantUid(tenantUid)
                                                .organizationUid(orgCompositeId.getUid())
                                                .organizationName(organization.getCommons().getLabel())
                                                .build())
                                .status(EventStatus.PENDING)
                                .createdAt(DateTimeUtils.nowIso())
                                .lastUpdatedAt(DateTimeUtils.nowIso())
                                .build();
                eventsInfraService.createEvent(sectorAuditEvent);

                eventAuditChannel.send(MessageBuilder.withPayload(KafkaConfig.AUDIT_WAKE_UP).build());

                return orgCompositeId;
        }

        @Override
        public List<Organization> findAllOrganizations(Long tenantId) {
                return organizationsInfraService.findAllOrganizations(tenantId);
        }

        @Override
        public Organization findOrganizationByTenantAndUid(Long tenantId, String orgUid, boolean fetchSectors)
                        throws FunctionalException {
                Organization org = organizationsInfraService.findOrganizationByUid(tenantId, orgUid);
                if (org == null) {
                        throw new FunctionalException(FunctionalErrorsTypes.ORG_NOT_FOUND.name(), null,
                                        messageSource.getMessage("org_not_found_by_uid", new Object[] { orgUid },
                                                        LocaleContextHolder.getLocale()));

                }
                if (fetchSectors) {
                        Sector sector = sectorsInfraService.fetchSectorsWithHierarchy(tenantId, org.getId());
                        org.setSector(sector);
                }
                return org;
        }

        @Override
        public Integer updateOrganization(String tenantUid, String orgUid, Organization organization)
                        throws FunctionalException {

                String callerName = this.getClass().getName() + "-updateOrganization";

                logService.infoS(callerName, "Update organization [%s] of tenant [%s]",
                                new Object[] { tenantUid, orgUid });

                // Find tenant
                Tenant tenant = tenantDomainService.findTenantByUid(tenantUid);

                // Find organization by tenant and uid
                Organization org = organizationsInfraService.findOrganizationByUid(tenant.getId(), orgUid);

                organization.setId(org.getId());
                organization.setTenantId(tenant.getId());
                organization.setUid(orgUid);

                Integer nbUpdated = organizationsInfraService.updateOrganization(tenant.getId(), organization.getId(),
                                organization.getCommons().getCode(),
                                organization.getCommons().getLabel(), organization.getCommons().getCountry(),
                                organization.getCommons().getStatus());

                // Create audit event
                AuditEvent orgUpdateAuditEvent = AuditEvent.builder()
                                .action(AuditAction.UPDATE)
                                .objectUid(organization.getUid())
                                .target(EventTarget.ORGANIZATION)
                                .scope(AuditScope.builder().tenantName(tenant.getLabel()).tenantUid(tenantUid)
                                                .organizationUid(organization.getUid())
                                                .organizationName(organization.getCommons().getLabel())
                                                .build())
                                .status(EventStatus.PENDING)
                                .createdAt(DateTimeUtils.nowIso())
                                .lastUpdatedAt(DateTimeUtils.nowIso())
                                .build();
                eventsInfraService.createEvent(orgUpdateAuditEvent);
                eventAuditChannel.send(MessageBuilder.withPayload(KafkaConfig.AUDIT_WAKE_UP).build());
                return nbUpdated;
        }

        @Override
        public List<Organization> findOrgsByIdList(List<Long> orgIds) {
                return organizationsInfraService.findOrgsByIdList(orgIds);
        }

        @Transactional
        @Override
        public Integer deleteOrganization(String tenantUid, String orgUid) throws FunctionalException {
                String callerName = this.getClass().getName() + "-deleteOrganization";

                // Find tenant
                Tenant tenant = tenantDomainService.findTenantByUid(tenantUid);

                // Find organization
                Organization organization = organizationsInfraService.findOrganizationByUid(tenant.getId(), orgUid);

                // Delete users
                Integer nbUsersDeleted = organizationsInfraService.deleteUsersByOrganization(tenant.getId(),
                                organization.getId());
                logService.debugS(callerName, "Nb of users deleted: [%s]", new Object[] { nbUsersDeleted });

                // Delete sectors
                Integer nbSectorsDeleted = organizationsInfraService.deleteSectors(tenant.getId(),
                                organization.getId());
                logService.debugS(callerName, "Nb of sectors deleted: [%s]", new Object[] { nbSectorsDeleted });

                // Delete organization
                Integer nbOrgDeleted = organizationsInfraService.deleteById(tenant.getId(), organization.getId());
                logService.debugS(callerName, "Nb of organizations deleted: [%s]", new Object[] { nbOrgDeleted });

                // Create audit event
                AuditEvent orgUpdateAuditEvent = AuditEvent.builder()
                                .action(AuditAction.DELETE)
                                .objectUid(organization.getUid())
                                .target(EventTarget.ORGANIZATION)
                                .scope(AuditScope.builder().tenantName(tenant.getLabel()).tenantUid(tenantUid)
                                                .organizationUid(organization.getUid())
                                                .organizationName(organization.getCommons().getLabel())
                                                .build())
                                .status(EventStatus.PENDING)
                                .createdAt(DateTimeUtils.nowIso())
                                .lastUpdatedAt(DateTimeUtils.nowIso())
                                .build();
                eventsInfraService.createEvent(orgUpdateAuditEvent);
                eventAuditChannel.send(MessageBuilder.withPayload(KafkaConfig.AUDIT_WAKE_UP).build());
                return nbUsersDeleted;
        }

}
