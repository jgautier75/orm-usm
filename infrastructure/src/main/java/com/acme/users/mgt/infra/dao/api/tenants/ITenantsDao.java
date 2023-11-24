package com.acme.users.mgt.infra.dao.api.tenants;

import java.util.List;

import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.users.mgt.infra.dto.tenants.v1.TenantDb;

public interface ITenantsDao {
    TenantDb findById(Long id);

    TenantDb findByUid(String uid);

    TenantDb findByCode(String code);

    CompositeId createTenant(String code, String label);

    Integer updateTenant(Long tenantId, String code, String label);

    Integer deleteTenant(Long tenantId);

    Boolean existsByCode(String code);

    List<TenantDb> findAllTenants();

    Integer deleteUsersByTenantId(Long tenantId);

    Integer deleteOrganizationsByTenantId(Long tenantId);

    Integer deleteSectorsByTenantId(Long tenantId);

}
