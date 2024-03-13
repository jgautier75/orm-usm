package com.acme.users.mgt.infra.services.impl.organizations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationsInfraService implements IOrganizationsInfraService {
    private static final String INSTRUMENTATION_NAME = OrganizationsInfraService.class.getCanonicalName();
    private final OrganizationsInfraConverter organizationsInfraConverter;
    private final IOrganizationsDao organizationsDao;
    private final SdkTracerProvider sdkTracerProvider;

    @Transactional
    @Override
    public CompositeId createOrganization(Organization organization) {
        OrganizationDb orgDb = organizationsInfraConverter.convertOrganizationToOrganizationDb(organization);
        return organizationsDao.createOrganization(orgDb);
    }

    @Override
    public List<Organization> findAllOrganizations(Long tenantId, Span parentSpan,Map<String,Object> searchParams) {
        Tracer tracer = sdkTracerProvider.get(INSTRUMENTATION_NAME);
        Span findSpan = tracer.spanBuilder("INFRA-FIND").setParent(Context.current().with(parentSpan)).startSpan();
        List<OrganizationDb> organizationDbs = null;
        try {
            organizationDbs = organizationsDao.findAllOrganizations(tenantId,searchParams);
        } catch (Exception t) {
            findSpan.setStatus(StatusCode.ERROR);
            findSpan.recordException(t);
            throw t;
        } finally {
            findSpan.end();
        }

        Span convertSpan = tracer.spanBuilder("INFRA-CONVERT").setParent(Context.current().with(parentSpan))
                .startSpan();
        List<Organization> orgs = new ArrayList<>();
        try {
            if (!CollectionUtils.isEmpty(organizationDbs)) {
                for (OrganizationDb orgDb : organizationDbs) {
                    orgs.add(organizationsInfraConverter.convertOrganizationDbToOrganization(orgDb));
                }
            }
        } catch (Exception t) {
            convertSpan.setStatus(StatusCode.ERROR);
            convertSpan.recordException(t);
            throw t;
        } finally {
            convertSpan.end();
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
    public Integer updateOrganization(Long tenantId, Long orgId, String code, String label, String country,
            OrganizationStatus status) {
        return organizationsDao.updateOrganization(tenantId, orgId, code, label, country, status);
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

    @Override
    public Integer deleteSectors(Long tenantId, Long orgId) {
        return organizationsDao.deleteSectorsByOrganization(tenantId, orgId);
    }

}
