package com.acme.users.mgt.infra.converters;

import org.springframework.stereotype.Component;
import com.acme.jga.users.mgt.dto.tenant.Tenant;
import com.acme.users.mgt.infra.dto.tenants.v1.TenantDb;

@Component
public class TenantsInfraConverter {

    public TenantDb tenantDomainToTenantDb(Tenant tenant) {
        TenantDb tenantDb = null;
        if (tenant != null) {
            tenantDb = TenantDb.builder()
                    .code(tenant.getCode())
                    .label(tenant.getLabel())
                    .build();
        }
        return tenantDb;
    }

    public Tenant tenantDbToTenantDomain(TenantDb tenantDb) {
        Tenant tenant = null;
        if (tenantDb != null) {
            tenant = Tenant.builder()
                    .code(tenantDb.getCode())
                    .id(tenantDb.getId())
                    .uid(tenantDb.getUid())
                    .label(tenantDb.getLabel())
                    .build();
        }
        return tenant;
    }

}
