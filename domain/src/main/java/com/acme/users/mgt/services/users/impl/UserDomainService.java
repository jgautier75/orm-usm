package com.acme.users.mgt.services.users.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.acme.jga.users.mgt.domain.events.v1.AuditAction;
import com.acme.jga.users.mgt.domain.events.v1.AuditEvent;
import com.acme.jga.users.mgt.domain.events.v1.AuditScope;
import com.acme.jga.users.mgt.domain.events.v1.EventStatus;
import com.acme.jga.users.mgt.domain.events.v1.EventTarget;
import com.acme.jga.users.mgt.domain.organizations.v1.Organization;
import com.acme.jga.users.mgt.domain.users.v1.User;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.dto.tenant.Tenant;
import com.acme.jga.users.mgt.exceptions.FunctionalErrorsTypes;
import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.jga.users.mgt.utils.DateTimeUtils;
import com.acme.users.mgt.config.KafkaConfig;
import com.acme.users.mgt.infra.services.api.events.IEventsInfraService;
import com.acme.users.mgt.infra.services.api.users.IUsersInfraService;
import com.acme.users.mgt.logging.services.api.ILogService;
import com.acme.users.mgt.services.organizations.api.IOrganizationsDomainService;
import com.acme.users.mgt.services.tenants.api.ITenantDomainService;
import com.acme.users.mgt.services.users.api.IUserDomainService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDomainService implements IUserDomainService {
    private final IOrganizationsDomainService organizationsDomainService;
    private final ITenantDomainService tenantDomainService;
    private final IUsersInfraService usersInfraService;
    private final ILogService logService;
    private final MessageSource messageSource;
    private final IEventsInfraService eventsInfraService;
    private final PublishSubscribeChannel eventAuditChannel;

    @Transactional
    @Override
    public CompositeId createUser(String tenantUid, String orgUid, User user) throws FunctionalException {
        String callerName = this.getClass().getName() + "-createUser";

        // Ensure email is not already in use
        logService.debugS(callerName, "Check if email [%s] is not already in use",
                new Object[] { user.getCredentials().getEmail() });
        Optional<Long> emailAlreadyExist = usersInfraService.emailUsed(user.getCredentials().getEmail());
        if (emailAlreadyExist.isPresent()) {
            throw new FunctionalException(FunctionalErrorsTypes.USER_EMAIL_ALREADY_USED.name(), null,
                    messageSource.getMessage("user_email_used",
                            new Object[] { user.getCredentials().getEmail() }, LocaleContextHolder.getLocale()));
        }

        // Ensure login is not already in use
        logService.debugS(callerName, "Check if login [%s] is not already in use",
                new Object[] { user.getCredentials().getLogin() });
        Optional<Long> loginAlreadyExist = usersInfraService.loginUsed(user.getCredentials().getLogin());
        if (loginAlreadyExist.isPresent()) {
            throw new FunctionalException(FunctionalErrorsTypes.USER_LOGIN_ALREADY_USED.name(), null,
                    messageSource.getMessage("user_login_used",
                            new Object[] { user.getCredentials().getEmail() }, LocaleContextHolder.getLocale()));
        }

        // Find tenant
        Tenant tenant = tenantDomainService.findTenantByUid(tenantUid);

        // Find organization
        Organization org = organizationsDomainService.findOrganizationByTenantAndUid(tenant.getId(), orgUid);

        logService.infoS(callerName, "Create user with login [%s] for tenant [%s] and organization [%s]",
                new Object[] { user.getCredentials().getLogin(), tenant.getCode(), org.getCommons().getCode() });

        user.setTenantId(tenant.getId());
        user.setOrganizationId(org.getId());
        CompositeId userCompositeId = usersInfraService.createUser(user);

        // Create user audit event
        AuditEvent userAuditEvent = AuditEvent.builder()
                .action(AuditAction.CREATE)
                .objectUid(userCompositeId.getUid())
                .target(EventTarget.USER)
                .infos(user.getCredentials().getLogin())
                .scope(AuditScope.builder().tenantName(tenant.getLabel()).tenantUid(tenantUid)
                        .organizationUid(org.getUid()).organizationName(org.getCommons().getLabel())
                        .build())
                .status(EventStatus.PENDING)
                .createdAt(DateTimeUtils.nowIso())
                .lastUpdatedAt(DateTimeUtils.nowIso())
                .build();
        eventsInfraService.createEvent(userAuditEvent);
        eventAuditChannel.send(MessageBuilder.withPayload(KafkaConfig.AUDIT_WAKE_UP).build());
        return userCompositeId;
    }

    @Transactional
    @Override
    public void updateUser(String tenantUid, String orgUid, User user) throws FunctionalException {

        // Find tenant
        Tenant tenant = tenantDomainService.findTenantByUid(tenantUid);

        // Find organization
        Organization org = organizationsDomainService.findOrganizationByTenantAndUid(tenant.getId(), orgUid);

        User rdbmsUser = usersInfraService.findByUid(tenant.getId(), org.getId(), user.getUid());
        if (rdbmsUser == null) {
            throw new FunctionalException(FunctionalErrorsTypes.USER_NOT_FOUND.name(), null, messageSource
                    .getMessage("user_not_found", new Object[] { user.getUid() }, LocaleContextHolder.getLocale()));
        }

        // Ensure email is not already in use
        Optional<Long> emailAlreadyExist = usersInfraService.emailUsed(user.getCredentials().getEmail());
        if (emailAlreadyExist.isPresent() && emailAlreadyExist.get().longValue() != rdbmsUser.getId().longValue()) {
            throw new FunctionalException(FunctionalErrorsTypes.USER_EMAIL_ALREADY_USED.name(), null,
                    messageSource.getMessage("user_email_used",
                            new Object[] { user.getCredentials().getEmail() }, LocaleContextHolder.getLocale()));
        }

        // Ensure login is not already in use
        Optional<Long> loginAlreadyExist = usersInfraService.loginUsed(user.getCredentials().getLogin());
        if (loginAlreadyExist.isPresent() && loginAlreadyExist.get().longValue() != rdbmsUser.getId().longValue()) {
            throw new FunctionalException(FunctionalErrorsTypes.USER_LOGIN_ALREADY_USED.name(), null,
                    messageSource.getMessage("user_login_used",
                            new Object[] { user.getCredentials().getEmail() }, LocaleContextHolder.getLocale()));
        }
        user.setId(rdbmsUser.getId());
        user.setOrganizationId(rdbmsUser.getOrganizationId());
        user.setTenantId(rdbmsUser.getTenantId());

        // Update user
        usersInfraService.updateUser(user);

        // Create user audit event
        AuditEvent userAuditEvent = AuditEvent.builder()
                .action(AuditAction.UPDATE)
                .objectUid(user.getUid())
                .target(EventTarget.USER)
                .infos(user.getCredentials().getLogin())
                .scope(AuditScope.builder().tenantName(tenant.getLabel()).tenantUid(tenantUid)
                        .organizationUid(org.getUid()).organizationName(org.getCommons().getLabel())
                        .build())
                .status(EventStatus.PENDING)
                .createdAt(DateTimeUtils.nowIso())
                .lastUpdatedAt(DateTimeUtils.nowIso())
                .build();
        eventsInfraService.createEvent(userAuditEvent);
        eventAuditChannel.send(MessageBuilder.withPayload(KafkaConfig.AUDIT_WAKE_UP).build());
    }

    @Override
    public List<User> findUsers(String tenantUid, String orgUid) throws FunctionalException {

        Long tenantId = null;
        Long orgId = null;
        if (!ObjectUtils.isEmpty(tenantUid)) {
            Tenant tenant = tenantDomainService.findTenantByUid(tenantUid);
            tenantId = tenant.getId();
        }

        if (!ObjectUtils.isEmpty(orgUid)) {
            Organization organization = organizationsDomainService.findOrganizationByTenantAndUid(tenantId, orgUid);
            orgId = organization.getId();
        } else {
            if (ObjectUtils.isEmpty(tenantUid)) {
                throw new FunctionalException(FunctionalErrorsTypes.TENANT_ORG_EXPECTED.name(), null,
                        messageSource.getMessage("tenant_org_filter_expected", null, LocaleContextHolder.getLocale()));
            }
        }
        List<User> users = usersInfraService.findUsers(tenantId, orgId);
        if (!CollectionUtils.isEmpty(users)) {
            List<Long> orgIds = users.stream().map(User::getOrganizationId).distinct().collect(Collectors.toList());
            List<Organization> organizations = organizationsDomainService.findOrgsByIdList(orgIds);
            for (User user : users) {
                Optional<Organization> userOrg = organizations.stream()
                        .filter(org -> org.getId().longValue() == user.getOrganizationId().longValue()).findFirst();
                if (userOrg.isPresent()) {
                    user.setOrganization(userOrg.get());
                }
            }
        }
        return users;
    }

    @Override
    public User findByUid(String tenantUid, String orgUid, String userUid) throws FunctionalException {
        // Find tenant
        Tenant tenant = tenantDomainService.findTenantByUid(tenantUid);

        // Find organization
        Organization org = organizationsDomainService.findOrganizationByTenantAndUid(tenant.getId(), orgUid);

        // Find user
        User user = usersInfraService.findByUid(tenant.getId(), org.getId(), userUid);
        if (user == null) {
            throw new FunctionalException(FunctionalErrorsTypes.USER_NOT_FOUND.name(), null,
                    messageSource.getMessage("user_not_found", null, LocaleContextHolder.getLocale()));
        }
        user.setOrganization(org);

        return user;
    }

    @Transactional
    @Override
    public Integer deleteUser(String tenantUid, String orgUid, String userUid) throws FunctionalException {
        // Find tenant
        Tenant tenant = tenantDomainService.findTenantByUid(tenantUid);

        // Find organization
        Organization org = organizationsDomainService.findOrganizationByTenantAndUid(tenant.getId(), orgUid);

        // Find user by uid
        User user = findByUid(tenantUid, orgUid, userUid);

        Integer nbRowsDeleted = usersInfraService.deleteUser(tenant.getId(), org.getId(), user.getId());

        // Create user audit event
        AuditEvent userAuditEvent = AuditEvent.builder()
                .action(AuditAction.DELETE)
                .objectUid(user.getUid())
                .target(EventTarget.USER)
                .infos(user.getCredentials().getLogin())
                .scope(AuditScope.builder().tenantName(tenant.getLabel()).tenantUid(tenantUid)
                        .organizationUid(org.getUid()).organizationName(org.getCommons().getLabel())
                        .build())
                .status(EventStatus.PENDING)
                .createdAt(DateTimeUtils.nowIso())
                .lastUpdatedAt(DateTimeUtils.nowIso())
                .build();
        eventsInfraService.createEvent(userAuditEvent);
        eventAuditChannel.send(MessageBuilder.withPayload(KafkaConfig.AUDIT_WAKE_UP).build());
        return nbRowsDeleted;
    }

}
