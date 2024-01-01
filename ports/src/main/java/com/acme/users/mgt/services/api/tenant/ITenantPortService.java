package com.acme.users.mgt.services.api.tenant;

import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.users.mgt.dto.port.shared.UidDto;
import com.acme.users.mgt.dto.port.tenants.v1.TenantDisplayDto;
import com.acme.users.mgt.dto.port.tenants.v1.TenantDto;
import com.acme.users.mgt.dto.port.tenants.v1.TenantListDisplayDto;

public interface ITenantPortService {

    /**
     * Create tenant.
     * 
     * @param tenantDto Tenant payload
     * @return Generated uid
     * @throws FunctionalException Functional error
     */
    UidDto createTenant(TenantDto tenantDto) throws FunctionalException;

    /**
     * Find tenant by uid.
     * 
     * @param uid Tenant uid
     * @return Tenant
     * @throws FunctionalException
     */
    TenantDisplayDto findTenantByUid(String uid) throws FunctionalException;

    /**
     * List all tenants for diaplay.
     * 
     * @return Tenants list
     */
    TenantListDisplayDto findAllTenants();

    /**
     * Update tenant.
     * 
     * @param uid       Tenant uid
     * @param tenantDto Tenant payload
     * @throws FunctionalException Functional error
     */
    Integer updateTenant(String uid, TenantDto tenantDto) throws FunctionalException;

    /**
     * Delete tenant and related data.
     * 
     * @param tenantUid Tenant uid
     * @throws FunctionalException Functional error
     */
    Integer deleteTenant(String tenantUid) throws FunctionalException;
}
