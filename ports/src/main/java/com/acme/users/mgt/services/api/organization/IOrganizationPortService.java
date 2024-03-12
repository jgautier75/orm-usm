package com.acme.users.mgt.services.api.organization;

import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.users.mgt.dto.port.organizations.v1.OrganizationDto;
import com.acme.users.mgt.dto.port.organizations.v1.OrganizationListLightDto;
import com.acme.users.mgt.dto.port.shared.UidDto;

import io.opentelemetry.api.trace.Span;

public interface IOrganizationPortService {

    /**
     * Create organization.
     * 
     * @param tenantUid       Tenant uid
     * @param organizationDto Organization payload
     * @param parentSpan Parent span
     * @return Generated uid
     * @throws FunctionalException Functional error
     */
    UidDto createOrganization(String tenantUid, OrganizationDto organizationDto, Span parentSpan) throws FunctionalException;

    /**
     * Find all organizations in a tenant.
     * 
     * @param tenantUid Tenant uid
     * @return Light representation of organization.
     * @throws FunctionalException Functional error
     */
    OrganizationListLightDto findAllOrgsLightByTenant(String tenantUid, Span parentSpan) throws FunctionalException;

    /**
     * Update organization.
     * 
     * @param tenantUid       Tenant uid
     * @param orgUid          Organization uid
     * @param organizationDto Organization payload
     * @throws FunctionalException Functional error
     */
    Integer updateOrganization(String tenantUid, String orgUid, OrganizationDto organizationDto)
            throws FunctionalException;

    /**
     * Find organization by uid.
     * 
     * @param tenantUid    Tenant uid
     * @param orgUid       Organization uid
     * @param fetchSectors Fectch sectors hierarchy
     * @return Organization
     * @throws FunctionalException
     */
    OrganizationDto findOrganizationByUid(String tenantUid, String orgUid, boolean fetchSectors)
            throws FunctionalException;

    /**
     * Delete organization.
     * 
     * @param tenantUid Tenant external id
     * @param orgUid    Organization external id
     * @return Nb od rows deleted
     * @throws FunctionalException Functional error
     */
    Integer deleteOrganization(String tenantUid, String orgUid) throws FunctionalException;

}
