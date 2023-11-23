package com.acme.users.mgt.infra.services.api.organizations;

import java.util.List;
import java.util.Optional;

import com.acme.jga.users.mgt.domain.organizations.v1.Organization;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.dto.organizations.OrganizationStatus;

public interface IOrganizationsInfraService {
    CompositeId createOrganization(Organization organization);

    List<Organization> findAllOrganizations(Long tenantId);

    Organization findOrganizationByUid(Long tenantId, String uid);

    void updateOrganization(Long tenantId, Long orgId, String code, String label, String country,
            OrganizationStatus status);

    Optional<Long> codeAlreadyUsed(String code);

    List<Organization> findOrgsByIdList(List<Long> orgIds);

    Integer deleteUsersByOrganization(Long tenantId, Long orgId);

    Integer deleteById(Long tenantId, Long orgId);
}