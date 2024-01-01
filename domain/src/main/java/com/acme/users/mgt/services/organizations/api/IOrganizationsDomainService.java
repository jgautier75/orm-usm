package com.acme.users.mgt.services.organizations.api;

import java.util.List;

import com.acme.jga.users.mgt.domain.organizations.v1.Organization;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.exceptions.FunctionalException;

public interface IOrganizationsDomainService {

    /**
     * Create organization.
     * 
     * @param tenantUid    Tenant external id
     * @param organization Organization
     * @return Composite id
     * @throws FunctionalException Functional error
     */
    CompositeId createOrganization(String tenantUid, Organization organization) throws FunctionalException;

    /**
     * List organizations.
     * 
     * @param tenantId Tenant internal id
     * @return Orgnizations list
     */
    List<Organization> findAllOrganizations(Long tenantId);

    /**
     * Find organizations.
     * 
     * @param tenantId Tenant internal id
     * @param orgUid   Organization external id
     * @return Organization
     * @throws FunctionalException Functional error
     */
    Organization findOrganizationByTenantAndUid(Long tenantId, String orgUid) throws FunctionalException;

    /**
     * Update organization.
     * 
     * @param tenantUid    Tenant external id
     * @param orgUid       Organization external id
     * @param organization Organization
     * @return Nb of rows updated
     * @throws FunctionalException Functional error
     */
    Integer updateOrganization(String tenantUid, String orgUid, Organization organization) throws FunctionalException;

    /**
     * Find organizations by external id list.
     * 
     * @param orgIds Organization id list
     * @return Organizations list
     */
    List<Organization> findOrgsByIdList(List<Long> orgIds);

    /**
     * Delete organization.
     * 
     * @param tenantUid Tenant external id
     * @param orgUid    Organization external id
     * @return Nb of rows deleted
     * @throws FunctionalException Functional error
     */
    Integer deleteOrganization(String tenantUid, String orgUid) throws FunctionalException;
}
