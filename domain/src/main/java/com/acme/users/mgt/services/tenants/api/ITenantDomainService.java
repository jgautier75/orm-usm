package com.acme.users.mgt.services.tenants.api;

import java.util.List;

import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.dto.tenant.Tenant;
import com.acme.jga.users.mgt.exceptions.FunctionalException;

public interface ITenantDomainService {

    CompositeId createTenant(Tenant tenant) throws FunctionalException;

    Tenant findTenantByUid(String uid) throws FunctionalException;

    List<Tenant> findAllTenants();

    Integer updateTenant(Tenant tenant) throws FunctionalException;

    Integer deleteTenant(String tenantUid) throws FunctionalException;

}
