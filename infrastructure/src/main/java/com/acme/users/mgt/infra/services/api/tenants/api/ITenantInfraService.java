package com.acme.users.mgt.infra.services.api.tenants.api;

import java.util.List;

import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.dto.tenant.Tenant;

public interface ITenantInfraService {
    CompositeId createTenant(Tenant tenant);

    Tenant findTenantByUid(String uid);

    boolean tenantExistsByCode(String code);

    List<Tenant> findAllTenants();

    Integer updateTenant(Tenant tenant);

    Integer deleteUsersByTenantId(Long tenantId);

    Integer deleteOrganizationsByTenantId(Long tenantId);

    Integer deleteTenant(Long tenantId);
}
