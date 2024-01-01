package com.acme.users.mgt.services.impl.tenant;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.dto.tenant.Tenant;
import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.users.mgt.converters.tenant.TenantsPortConverter;
import com.acme.users.mgt.dto.port.shared.UidDto;
import com.acme.users.mgt.dto.port.tenants.v1.TenantDisplayDto;
import com.acme.users.mgt.dto.port.tenants.v1.TenantDto;
import com.acme.users.mgt.dto.port.tenants.v1.TenantListDisplayDto;
import com.acme.users.mgt.services.api.tenant.ITenantPortService;
import com.acme.users.mgt.services.tenants.api.ITenantDomainService;
import com.acme.users.mgt.validation.ValidationException;
import com.acme.users.mgt.validation.ValidationResult;
import com.acme.users.mgt.validation.tenants.TenantsValidationEngine;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantPortService implements ITenantPortService {
    private final TenantsPortConverter tenantsConverter;
    private final ITenantDomainService tenantDomainService;
    private final TenantsValidationEngine tenantsValidationEngine;

    /**
     * @inheritDoc
     */
    @Override
    public UidDto createTenant(TenantDto tenantDto) throws FunctionalException {
        ValidationResult validationResult = tenantsValidationEngine.validate(tenantDto);
        if (!validationResult.isSuccess()) {
            throw new ValidationException(validationResult.getErrors());
        }
        Tenant tenant = tenantsConverter.tenantDtoToDomainTenant(tenantDto);
        CompositeId compositeId = tenantDomainService.createTenant(tenant);
        return new UidDto(compositeId.getUid());
    }

    @Override
    public TenantDisplayDto findTenantByUid(String uid) throws FunctionalException {
        Tenant tenant = tenantDomainService.findTenantByUid(uid);
        TenantDisplayDto tenantDisplayDto = null;
        if (tenant != null) {
            tenantDisplayDto = tenantsConverter.tenantDomainToDisplay(tenant);
        }
        return tenantDisplayDto;
    }

    @Override
    public TenantListDisplayDto findAllTenants() {
        List<Tenant> tenants = tenantDomainService.findAllTenants();
        List<TenantDisplayDto> tenantDisplayDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(tenants)) {
            for (Tenant tenant : tenants) {
                tenantDisplayDtos.add(tenantsConverter.tenantDomainToDisplay(tenant));
            }
        }
        return new TenantListDisplayDto(tenantDisplayDtos);
    }

    @Override
    public Integer updateTenant(String uid, TenantDto tenantDto) throws FunctionalException {
        Tenant tenant = tenantsConverter.tenantDtoToDomainTenant(tenantDto);
        tenant.setUid(uid);
        return tenantDomainService.updateTenant(tenant);
    }

    @Override
    public Integer deleteTenant(String tenantUid) throws FunctionalException {

        // Ensure tenant exists
        findTenantByUid(tenantUid);

        return tenantDomainService.deleteTenant(tenantUid);
    }

}
