package com.acme.users.mgt.infra.dao.api.organizations;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.acme.jga.users.mgt.domain.pagination.PaginatedResults;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.dto.organizations.OrganizationStatus;
import com.acme.users.mgt.infra.dto.organizations.v1.OrganizationDb;

public interface IOrganizationsDao {

	CompositeId createOrganization(OrganizationDb org);

	OrganizationDb findOrganizationByTenantAndId(Long tenantId, Long id);

	OrganizationDb findOrganizationByTenantAndUid(Long tenantId, String uid);

	Integer updateOrganization(Long tenantId, Long orgId, String code, String label, String country,
			OrganizationStatus status);

	Integer deleteOrganization(Long tenantId, Long orgId);

	PaginatedResults<OrganizationDb> findAllOrganizations(Long tenantId,Map<String,Object> searchParams);

	Optional<Long> existsByCode(String code);

	List<OrganizationDb> findOrgsByIdList(List<Long> orgIds);

	Integer deleteUsersByOrganization(Long tenantId, Long orgId);

	Integer deleteById(Long tenantId, Long orgId);

	Integer deleteSectorsByOrganization(Long tenantId, Long orgId);

}
