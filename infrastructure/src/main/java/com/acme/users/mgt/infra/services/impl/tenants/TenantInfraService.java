package com.acme.users.mgt.infra.services.impl.tenants;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.dto.tenant.Tenant;
import com.acme.users.mgt.infra.converters.TenantsInfraConverter;
import com.acme.users.mgt.infra.dao.api.tenants.ITenantsDao;
import com.acme.users.mgt.infra.dto.tenants.v1.TenantDb;
import com.acme.users.mgt.infra.services.api.tenants.api.ITenantInfraService;
import com.acme.users.mgt.logging.services.api.ILogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantInfraService implements ITenantInfraService {
    private final ITenantsDao tenantsDao;
    private final TenantsInfraConverter tenantsInfraConverter;
    private final ILogService logService;

    @Override
    public CompositeId createTenant(Tenant tenant) {
        return tenantsDao.createTenant(tenant.getCode(), tenant.getLabel());
    }

    @Override
    public Tenant findTenantByUid(String uid) {
        TenantDb tenantDb = tenantsDao.findByUid(uid);
        Tenant tenant = null;
        if (tenantDb != null) {
            tenant = tenantsInfraConverter.tenantDbToTenantDomain(tenantDb);
        }
        return tenant;
    }

    @Override
    public boolean tenantExistsByCode(String code) {
        return tenantsDao.existsByCode(code);
    }

    @Override
    public List<Tenant> findAllTenants() {
        List<TenantDb> tenantDbs = tenantsDao.findAllTenants();
        List<Tenant> tenants = new ArrayList<>();
        if (!CollectionUtils.isEmpty(tenantDbs)) {
            for (TenantDb tenantDb : tenantDbs) {
                tenants.add(tenantsInfraConverter.tenantDbToTenantDomain(tenantDb));
            }
        }
        return tenants;
    }

    @Override
    public void updateTenant(Tenant tenant) {
        tenantsDao.updateTenant(tenant.getId(), tenant.getCode(), tenant.getLabel());
        logService.debugS(this.getClass().getCanonicalName() + "-updateTenant",
                "tenant " + tenant.getCode() + " updated", null);
    }

    @Override
    public Integer deleteUsersByTenantId(Long tenantId) {
        return tenantsDao.deleteUsersByTenantId(tenantId);
    }

    @Override
    public Integer deleteOrganizationsByTenantId(Long tenantId) {
        return tenantsDao.deleteOrganizationsByTenantId(tenantId);
    }

    @Override
    public Integer deleteTenant(Long tenantId) {
        return tenantsDao.deleteTenant(tenantId);
    }

}
