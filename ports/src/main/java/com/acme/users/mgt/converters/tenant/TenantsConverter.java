package com.acme.users.mgt.converters.tenant;

import org.springframework.stereotype.Component;

import com.acme.jga.users.mgt.dto.tenant.Tenant;
import com.acme.users.mgt.dto.port.tenants.v1.TenantDisplayDto;
import com.acme.users.mgt.dto.port.tenants.v1.TenantDto;

@Component
public class TenantsConverter {

    public Tenant tenantDtoToDomainTenant(TenantDto tenantDto) {
        Tenant tenant = null;
        if (tenantDto != null) {
            tenant = Tenant.builder()
                    .label(tenantDto.getLabel())
                    .code(tenantDto.getCode())
                    .build();
        }
        return tenant;
    }

    public TenantDisplayDto tenantDomainToDisplay(Tenant tenant) {
        TenantDisplayDto displayDto = null;
        if (tenant != null) {
            displayDto = new TenantDisplayDto(tenant.getUid(), tenant.getCode(), tenant.getLabel());
        }

        return displayDto;
    }

}
