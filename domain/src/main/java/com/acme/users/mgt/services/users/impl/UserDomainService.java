package com.acme.users.mgt.services.users.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import com.acme.jga.users.mgt.domain.organizations.v1.Organization;
import com.acme.jga.users.mgt.domain.users.v1.User;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.dto.tenant.Tenant;
import com.acme.jga.users.mgt.exceptions.FunctionalErrorsTypes;
import com.acme.jga.users.mgt.exceptions.FunctionalException;
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

    @Override
    public CompositeId createUser(String tenantUid, String orgUid, User user) throws FunctionalException {
        String callerName = this.getClass().getName() + "-createUser";

        // Ensure email is not already in use
        Optional<Long> emailAlreadyExist = usersInfraService.emailUsed(user.getCredentials().getEmail());
        if (emailAlreadyExist.isPresent()) {
            throw new FunctionalException(FunctionalErrorsTypes.USER_EMAIL_ALREADY_USED.name(), null,
                    messageSource.getMessage("user_email_used",
                            new Object[] { user.getCredentials().getEmail() }, LocaleContextHolder.getLocale()));
        }

        // Ensure login is not already in use
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
        return usersInfraService.createUser(user);
    }

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

        // Update user
        usersInfraService.updateUser(user);
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

        return user;
    }

    @Override
    public Integer deleteUser(String tenantUid, String orgUid, String userUid) throws FunctionalException {
        // Find tenant
        Tenant tenant = tenantDomainService.findTenantByUid(tenantUid);

        // Find organization
        Organization org = organizationsDomainService.findOrganizationByTenantAndUid(tenant.getId(), orgUid);

        // Find user by uid
        User user = findByUid(tenantUid, orgUid, userUid);

        return usersInfraService.deleteUser(tenant.getId(), org.getId(), user.getId());
    }

}
