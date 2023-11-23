package com.acme.users.mgt.services.organizations.api;

import java.util.List;

import com.acme.jga.users.mgt.domain.organizations.v1.Organization;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.exceptions.FunctionalException;

public interface IOrganizationsDomainService {

    CompositeId createOrganization(String tenantUid, Organization organization) throws FunctionalException;

    List<Organization> findAllOrganizations(Long tenantId);

    Organization findOrganizationByTenantAndUid(Long tenantId, String orgUid) throws FunctionalException;

    void updateOrganization(String tenantUid, String orgUid, Organization organization) throws FunctionalException;

    List<Organization> findOrgsByIdList(List<Long> orgIds);

    void deleteOrganization(String tenantUid, String orgUid) throws FunctionalException;
}
