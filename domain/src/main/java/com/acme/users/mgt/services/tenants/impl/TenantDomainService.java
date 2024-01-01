package com.acme.users.mgt.services.tenants.impl;

import java.util.List;
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
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.dto.tenant.Tenant;
import com.acme.jga.users.mgt.exceptions.FunctionalErrorsTypes;
import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.jga.users.mgt.utils.DateTimeUtils;
import com.acme.users.mgt.config.KafkaConfig;
import com.acme.users.mgt.infra.services.api.events.IEventsInfraService;
import com.acme.users.mgt.infra.services.api.tenants.api.ITenantInfraService;
import com.acme.users.mgt.logging.services.api.ILogService;
import com.acme.users.mgt.services.tenants.api.ITenantDomainService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TenantDomainService implements ITenantDomainService {
    private final ITenantInfraService tenantInfraService;
    private final ILogService logService;
    private final MessageSource messageSource;
    private final IEventsInfraService eventsInfraService;
    private final PublishSubscribeChannel eventAuditChannel;

    @Override
    public CompositeId createTenant(Tenant tenant) throws FunctionalException {
        String callerName = this.getClass().getName() + "-createTenant";
        boolean alreadyExist = tenantInfraService.tenantExistsByCode(tenant.getCode());
        if (alreadyExist) {
            throw new FunctionalException(FunctionalErrorsTypes.TENANT_CODE_ALREADY_USED.name(), null,
                    messageSource.getMessage("tenant_code_already_used",
                            new Object[] { tenant.getCode() }, LocaleContextHolder.getLocale()));
        }
        CompositeId compositeId = tenantInfraService.createTenant(tenant);
        logService.infoS(callerName, "Created tenant [%s]", new Object[] { compositeId.getUid() });

        // Create audit event
        AuditEvent auditEvent = AuditEvent.builder()
                .action(AuditAction.CREATE)
                .objectUid(compositeId.getUid())
                .target(EventTarget.TENANT)
                .scope(AuditScope.builder().tenantName(tenant.getLabel()).tenantUid(compositeId.getUid()).build())
                .status(EventStatus.PENDING)
                .createdAt(DateTimeUtils.nowIso())
                .lastUpdatedAt(DateTimeUtils.nowIso())
                .build();
        String eventUid = eventsInfraService.createEvent(auditEvent);
        logService.debugS(callerName, "Created event [%s]", new Object[] { eventUid });
        return compositeId;
    }

    @Override
    public Tenant findTenantByUid(String uid) throws FunctionalException {
        Tenant tenant = tenantInfraService.findTenantByUid(uid);
        if (tenant == null) {
            throw new FunctionalException(FunctionalErrorsTypes.TENANT_NOT_FOUND.name(), null,
                    messageSource.getMessage("tenant_not_found_by_uid", new Object[] { uid },
                            LocaleContextHolder.getLocale()));
        }
        return tenant;
    }

    @Override
    public List<Tenant> findAllTenants() {
        return tenantInfraService.findAllTenants();
    }

    @Override
    public Integer updateTenant(Tenant tenant) throws FunctionalException {
        String callerName = this.getClass().getName() + "-updateTenant";

        logService.infoS(callerName, "U%pdating tenant [%s] ", new Object[] { tenant.getUid() });

        // Ensure tenant already exists
        Tenant rbdmsTenant = findTenantByUid(tenant.getUid());
        tenant.setId(rbdmsTenant.getId());

        // Tenant update
        Integer nbRowsUpdated = tenantInfraService.updateTenant(tenant);

        // Create audit event
        AuditEvent auditEvent = AuditEvent.builder()
                .action(AuditAction.UPDATE)
                .objectUid(tenant.getUid())
                .target(EventTarget.TENANT)
                .scope(AuditScope.builder().tenantName(tenant.getLabel()).tenantUid(tenant.getUid()).build())
                .status(EventStatus.PENDING)
                .createdAt(DateTimeUtils.nowIso())
                .lastUpdatedAt(DateTimeUtils.nowIso())
                .build();
        String eventUid = eventsInfraService.createEvent(auditEvent);
        logService.debugS(callerName, "Event [%s] created", new Object[] { eventUid });

        return nbRowsUpdated;
    }

    @Override
    public Integer deleteTenant(String tenantUid) throws FunctionalException {
        String callerName = this.getClass().getName() + "-deleteTenant";
        logService.infoS(callerName, "Delete tenant [%s]", new Object[] { tenantUid });

        // Find tenant
        Tenant tenant = findTenantByUid(tenantUid);

        // Delete users by tenantId
        logService.debugS(callerName, "Delete users for tenant [%s]", new Object[] { tenantUid });
        tenantInfraService.deleteUsersByTenantId(tenant.getId());

        // Delete sectors by tenant id
        logService.debugS(callerName, "Delete sectors for tenant [%s]", new Object[] { tenantUid });
        tenantInfraService.deleteSectorsByTenantId(tenant.getId());

        // Delete organizations by tenant id
        logService.debugS(callerName, "Delete organizations for tenant [%s]", new Object[] { tenantUid });
        tenantInfraService.deleteOrganizationsByTenantId(tenant.getId());

        // Delete tenant
        logService.debugS(callerName, "Delete tenant [%s] itself", new Object[] { tenantUid });
        Integer nbDeleted = tenantInfraService.deleteTenant(tenant.getId());

        // Create audit event
        AuditEvent auditEvent = AuditEvent.builder()
                .action(AuditAction.DELETE)
                .objectUid(tenant.getUid())
                .target(EventTarget.TENANT)
                .scope(AuditScope.builder().tenantName(tenant.getLabel()).tenantUid(tenant.getUid()).build())
                .status(EventStatus.PENDING)
                .createdAt(DateTimeUtils.nowIso())
                .lastUpdatedAt(DateTimeUtils.nowIso())
                .build();
        eventsInfraService.createEvent(auditEvent);
        eventAuditChannel.send(MessageBuilder.withPayload(KafkaConfig.AUDIT_WAKE_UP).build());
        return nbDeleted;
    }

}
