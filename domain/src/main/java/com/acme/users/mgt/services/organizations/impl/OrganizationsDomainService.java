package com.acme.users.mgt.services.organizations.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.acme.jga.users.mgt.domain.events.v1.AuditAction;
import com.acme.jga.users.mgt.domain.events.v1.AuditChange;
import com.acme.jga.users.mgt.domain.events.v1.AuditEvent;
import com.acme.jga.users.mgt.domain.events.v1.AuditScope;
import com.acme.jga.users.mgt.domain.events.v1.EventStatus;
import com.acme.jga.users.mgt.domain.events.v1.EventTarget;
import com.acme.jga.users.mgt.domain.organizations.v1.Organization;
import com.acme.jga.users.mgt.domain.pagination.PaginatedResults;
import com.acme.jga.users.mgt.domain.sectors.v1.Sector;
import com.acme.jga.users.mgt.dto.filtering.FilteringConstants;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.dto.tenant.Tenant;
import com.acme.jga.users.mgt.exceptions.FunctionalErrorsTypes;
import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.jga.users.mgt.utils.DateTimeUtils;
import com.acme.users.mgt.config.KafkaConfig;
import com.acme.users.mgt.events.EventBuilderOrganization;
import com.acme.users.mgt.infra.services.api.events.IEventsInfraService;
import com.acme.users.mgt.infra.services.api.organizations.IOrganizationsInfraService;
import com.acme.users.mgt.infra.services.api.sectors.ISectorsInfraService;
import com.acme.users.mgt.logging.services.api.ILogService;
import com.acme.users.mgt.services.organizations.api.IOrganizationsDomainService;
import com.acme.users.mgt.services.tenants.api.ITenantDomainService;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.Context;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationsDomainService implements IOrganizationsDomainService {
        private static final String INSTRUMENTATION_NAME = OrganizationsDomainService.class.getCanonicalName();
        private final IOrganizationsInfraService organizationsInfraService;
        private final ITenantDomainService tenantDomainService;
        private final MessageSource messageSource;
        private final ILogService logService;
        private final ISectorsInfraService sectorsInfraService;
        private final IEventsInfraService eventsInfraService;
        private final PublishSubscribeChannel eventAuditChannel;
        @Autowired
        private TracerProvider sdkTracerProvider;      
        private final EventBuilderOrganization eventBuilderOrganization;

        public void setSdkTracerProvider(TracerProvider sdkTracerProvider) {
                this.sdkTracerProvider = sdkTracerProvider;
        }        

        @Transactional
        @Override
        public CompositeId createOrganization(String tenantUid, Organization organization, Span parentSpan)
                        throws FunctionalException {
                // Find tenant
                Tracer tracer = sdkTracerProvider.get(INSTRUMENTATION_NAME);
                Span tenantSpan = tracer.spanBuilder("DOMAIN_FIND_TENANT")
                                .setParent(Context.current().with(parentSpan))
                                .startSpan();
                Tenant tenant = null;
                try {
                        tenant = tenantDomainService.findTenantByUid(tenantUid);
                } catch (Exception e) {
                        tenantSpan.setStatus(StatusCode.ERROR);
                        tenantSpan.recordException(e);
                        throw e;
                } finally {
                        tenantSpan.end();
                }

                // Ensure code is not already in used
                Span codeSpan = tracer.spanBuilder("DOMAIN_ORG_CODE_EXISTS")
                                .setParent(Context.current().with(tenantSpan))
                                .startSpan();
                Optional<Long> orgCodeUsed = Optional.empty();
                try {
                        orgCodeUsed = organizationsInfraService
                                        .codeAlreadyUsed(organization.getCommons().getCode());
                } catch (Exception e) {
                        codeSpan.setStatus(StatusCode.ERROR);
                        codeSpan.recordException(e);
                        throw e;
                } finally {
                        codeSpan.end();
                }
                if (orgCodeUsed.isPresent()) {
                        throw new FunctionalException(FunctionalErrorsTypes.ORG_CODE_ALREADY_USED.name(), null,
                                        messageSource.getMessage("org_code_already_used",
                                                        new Object[] { organization.getCommons().getCode() },
                                                        LocaleContextHolder.getLocale()));
                }

                organization.setTenantId(tenant.getId());

                Span infraSpan = tracer.spanBuilder("DOMAIN_INFRA_CREATE").setParent(Context.current().with(codeSpan))
                                .startSpan();
                CompositeId orgCompositeId = null;
                try {
                        orgCompositeId = organizationsInfraService.createOrganization(organization);
                } catch (Exception e) {
                        infraSpan.setStatus(StatusCode.ERROR);
                        infraSpan.recordException(e);
                        throw e;
                } finally {
                        infraSpan.end();
                }

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
                Span orgEventSpan = tracer.spanBuilder("DOMAIN_ORG_EVENT")
                                .setParent(Context.current().with(infraSpan))
                                .startSpan();
                try {
                        eventsInfraService.createEvent(orgAuditEvent);
                } catch (Exception e) {
                        orgEventSpan.setStatus(StatusCode.ERROR);
                        orgEventSpan.recordException(e);
                        throw e;
                } finally {
                        orgEventSpan.end();
                }

                // Create root sector
                Sector sector = Sector.builder()
                                .code(organization.getCommons().getCode())
                                .label(organization.getCommons().getLabel())
                                .orgId(orgCompositeId.getId())
                                .root(true)
                                .tenantId(tenant.getId())
                                .build();
                CompositeId sectorCompositeId = null;

                Span sectorSpan = tracer.spanBuilder("DOMAIN_SECTOR").setParent(Context.current().with(orgEventSpan))
                                .startSpan();
                try {
                        sectorCompositeId = sectorsInfraService.createSector(tenant.getId(), orgCompositeId.getId(),
                                        sector);
                } catch (Exception e) {
                        sectorSpan.setStatus(StatusCode.ERROR);
                        sectorSpan.recordException(e);
                        throw e;
                } finally {
                        sectorSpan.end();
                }

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

                Span sectorEventSpan = tracer.spanBuilder("DOMAIN_SECTOR_EVENT")
                                .setParent(Context.current().with(sectorSpan))
                                .startSpan();
                try {
                        eventsInfraService.createEvent(sectorAuditEvent);
                } catch (Exception e) {
                        sectorEventSpan.setStatus(StatusCode.ERROR);
                        sectorEventSpan.recordException(e);
                        throw e;
                } finally {
                        sectorEventSpan.end();

                }

                Span eventPubSpan = tracer.spanBuilder("DOMAIN_EVENT_PUBLISH")
                                .setParent(Context.current().with(sectorEventSpan))
                                .startSpan();
                try {
                        eventAuditChannel.send(MessageBuilder.withPayload(KafkaConfig.AUDIT_WAKE_UP).build());
                } catch (Exception e) {
                        eventPubSpan.setStatus(StatusCode.ERROR);
                        eventPubSpan.recordException(e);
                        throw e;
                } finally {
                        eventPubSpan.end();
                }

                return orgCompositeId;
        }

        @Override
        public PaginatedResults<Organization> findAllOrganizations(Long tenantId, Span parentSpan, Map<String,Object> searchParams) {
                Tracer tracer = sdkTracerProvider.get(INSTRUMENTATION_NAME);
                Span domainSpan = tracer.spanBuilder("DOMAIN")
                                .setParent(Context.current().with(parentSpan))
                                .startSpan();
                Integer pageIndex =  (Integer)searchParams.get(FilteringConstants.PAGE_INDEX);
                searchParams.put(FilteringConstants.PAGE_INDEX, pageIndex);
                try {
                        return organizationsInfraService.findAllOrganizations(tenantId, parentSpan,searchParams);
                } catch (Exception t) {
                        domainSpan.setStatus(StatusCode.ERROR);
                        domainSpan.recordException(t);
                        throw t;
                } finally {
                        domainSpan.end();
                }
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

        @Transactional
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

                // Build audit changes
                List<AuditChange> auditChanges = eventBuilderOrganization.buildAuditsChange(org.getCommons(),
                                organization.getCommons());
                boolean anythingChanged = !auditChanges.isEmpty();
                int nbUpdated = 0;

                if (anythingChanged) {
                        nbUpdated = organizationsInfraService.updateOrganization(tenant.getId(),
                                        organization.getId(),
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
                        if (!CollectionUtils.isEmpty(auditChanges)) {
                                orgUpdateAuditEvent.setChanges(auditChanges);

                        }
                        eventsInfraService.createEvent(orgUpdateAuditEvent);
                        eventAuditChannel.send(MessageBuilder.withPayload(KafkaConfig.AUDIT_WAKE_UP).build());

                }
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
                AuditEvent orgDeleteAuditEvent = AuditEvent.builder()
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
                eventsInfraService.createEvent(orgDeleteAuditEvent);
                eventAuditChannel.send(MessageBuilder.withPayload(KafkaConfig.AUDIT_WAKE_UP).build());
                return nbUsersDeleted;
        }

}
