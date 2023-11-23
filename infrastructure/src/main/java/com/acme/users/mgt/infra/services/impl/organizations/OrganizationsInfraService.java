package com.acme.users.mgt.infra.services.impl.organizations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import com.acme.jga.users.mgt.domain.organizations.v1.Organization;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.dto.organizations.OrganizationStatus;
import com.acme.users.mgt.infra.converters.OrganizationsInfraConverter;
import com.acme.users.mgt.infra.dao.api.organizations.IOrganizationsDao;
import com.acme.users.mgt.infra.dto.organizations.v1.OrganizationDb;
import com.acme.users.mgt.infra.services.api.organizations.IOrganizationsInfraService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationsInfraService implements IOrganizationsInfraService {
    private final OrganizationsInfraConverter organizationsInfraConverter;
    private final IOrganizationsDao organizationsDao;

    @Transactional
    @Override
    public CompositeId createOrganization(Organization organization) {
        OrganizationDb orgDb = organizationsInfraConverter.convertOrganizationToOrganizationDb(organization);
        return organizationsDao.createOrganization(orgDb);
    }

    @Override
    public List<Organization> findAllOrganizations(Long tenantId) {
        List<OrganizationDb> organizationDbs = organizationsDao.findAllOrganizations(tenantId);
        List<Organization> orgs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(organizationDbs)) {
            for (OrganizationDb orgDb : organizationDbs) {
                orgs.add(organizationsInfraConverter.convertOrganizationDbToOrganization(orgDb));
            }
        }
        return orgs;
    }

    @Override
    public Organization findOrganizationByUid(Long tenantId, String uid) {
        OrganizationDb orgDb = organizationsDao.findOrganizationByTenantAndUid(tenantId, uid);
        return organizationsInfraConverter.convertOrganizationDbToOrganization(orgDb);
    }

    @Transactional
    @Override
    public void updateOrganization(Long tenantId, Long orgId, String code, String label, String country,
            OrganizationStatus status) {
        organizationsDao.updateOrganization(tenantId, orgId, code, label, country, status);
    }

    @Override
    public Optional<Long> codeAlreadyUsed(String code) {
        return organizationsDao.existsByCode(code);
    }

    @Override
    public List<Organization> findOrgsByIdList(List<Long> orgIds) {
        List<OrganizationDb> orgDbs = organizationsDao.findOrgsByIdList(orgIds);
        List<Organization> orgs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(orgIds)) {
            for (OrganizationDb orgDb : orgDbs) {
                orgs.add(organizationsInfraConverter.convertOrganizationDbToOrganization(orgDb));
            }
        }
        return orgs;
    }

    @Override
    public Integer deleteById(Long tenantId, Long orgId) {
        return organizationsDao.deleteById(tenantId, orgId);
    }

    @Override
    public Integer deleteUsersByOrganization(Long tenantId, Long orgId) {
        return organizationsDao.deleteUsersByOrganization(tenantId, orgId);
    }

}
