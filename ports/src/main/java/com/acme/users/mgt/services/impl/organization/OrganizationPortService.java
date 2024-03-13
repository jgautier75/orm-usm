package com.acme.users.mgt.services.impl.organization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.acme.jga.search.filtering.parser.QueryParser;
import com.acme.jga.search.filtering.utils.ParsingResult;
import com.acme.jga.users.mgt.domain.organizations.v1.Organization;
import com.acme.jga.users.mgt.domain.pagination.PaginatedResults;
import com.acme.jga.users.mgt.dto.filtering.FilteringConstants;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.dto.tenant.Tenant;
import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.users.mgt.converters.organization.OrganizationsPortConverter;
import com.acme.users.mgt.dto.port.organizations.v1.OrganizationDto;
import com.acme.users.mgt.dto.port.organizations.v1.OrganizationLightDto;
import com.acme.users.mgt.dto.port.organizations.v1.OrganizationListLightDto;
import com.acme.users.mgt.dto.port.search.SearchFilterDto;
import com.acme.users.mgt.dto.port.shared.UidDto;
import com.acme.users.mgt.services.api.organization.IOrganizationPortService;
import com.acme.users.mgt.services.organizations.api.IOrganizationsDomainService;
import com.acme.users.mgt.services.tenants.api.ITenantDomainService;
import com.acme.users.mgt.validation.ValidationException;
import com.acme.users.mgt.validation.ValidationResult;
import com.acme.users.mgt.validation.organizations.OrganizationsValidationEngine;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.Context;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationPortService implements IOrganizationPortService {
    private static final String INSTRUMENTATION_NAME = OrganizationPortService.class.getCanonicalName();
    private final ITenantDomainService tenantDomainService;
    private final IOrganizationsDomainService organizationDomainService;
    private final OrganizationsPortConverter organizationsConverter;
    private final OrganizationsValidationEngine organizationsValidationEngine;
    private final QueryParser queryParser = new QueryParser();
    @Autowired
    private TracerProvider sdkTracerProvider;

     public void setSdkTracerProvider(TracerProvider sdkTracerProvider) {
        this.sdkTracerProvider = sdkTracerProvider;
     }  

    /**
     * @inheritDoc
     */
    @Override
    public UidDto createOrganization(String tenantUid, OrganizationDto organizationDto, Span parentSpan) throws FunctionalException {

        // Validate payload
        ValidationResult validationResult = organizationsValidationEngine.validate(organizationDto);
        if (!validationResult.isSuccess()) {
            throw new ValidationException(validationResult.getErrors());
        }

        // Convert to domain format
        Organization org = organizationsConverter.convertOrganizationDtoToDomain(organizationDto);

        // Create organization
       
        CompositeId compositeId = organizationDomainService.createOrganization(tenantUid, org, parentSpan);
        return new UidDto(compositeId.getUid());
    }

    /**
     * @inheritDoc
     */
    @Override
    public OrganizationListLightDto findAllOrgsLightByTenant(String tenantUid, Span parentSpan,SearchFilterDto searchFilterDto)
            throws FunctionalException {
        Tracer tracer = sdkTracerProvider.get(INSTRUMENTATION_NAME);
        // Find tenant
        Span tenantSpan = tracer.spanBuilder("TENANT")
                .setParent(Context.current().with(parentSpan))
                .startSpan();
        

        Tenant tenant = null;
        try {
            tenant = tenantDomainService.findTenantByUid(tenantUid);
        } catch (Exception t) {
            tenantSpan.setStatus(StatusCode.ERROR);
            tenantSpan.recordException(t);
            throw t;
        } finally {
            tenantSpan.end();
        }

        ParsingResult parsingResult = queryParser.parseQuery(searchFilterDto.getFilter());
        Map<String,Object> searchParams = new HashMap<>();
        searchParams.put(FilteringConstants.PAGE_INDEX, searchFilterDto.getPageIndex());
        searchParams.put(FilteringConstants.PAGE_SIZE, searchFilterDto.getPageSize());
        searchParams.put(FilteringConstants.PARSING_RESULTS, parsingResult);
        searchParams.put(FilteringConstants.ORDER_BY, searchFilterDto.getOrderBy());

        PaginatedResults<Organization> paginatedResults = organizationDomainService.findAllOrganizations(tenant.getId(), parentSpan,searchParams);
        List<OrganizationLightDto> lightOrgs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(paginatedResults.getResults())) {
            for (Organization org : paginatedResults.getResults()) {
                lightOrgs.add(organizationsConverter.convertOrganizationToLightOrgDto(org));
            }
        }
        return new OrganizationListLightDto(paginatedResults.getNbResults(),paginatedResults.getNbPages(), lightOrgs);
    }

    /**
     * @inheritDoc
     */
    @Override
    public OrganizationDto findOrganizationByUid(String tenantUid, String orgUid, boolean fetchSectors)
            throws FunctionalException {
        // Find tenant
        Tenant tenant = tenantDomainService.findTenantByUid(tenantUid);

        // Find organization
        Organization org = organizationDomainService.findOrganizationByTenantAndUid(tenant.getId(), orgUid,
                fetchSectors);
        OrganizationDto organizationDto = organizationsConverter.convertOrganizationToDto(org);
        organizationDto.setTenantUid(tenantUid);
        return organizationDto;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Integer updateOrganization(String tenantUid, String orgUid, OrganizationDto organizationDto)
            throws FunctionalException {
        // Find tenant
        tenantDomainService.findTenantByUid(tenantUid);

        // Update organization
        Organization org = organizationsConverter.convertOrganizationDtoToDomain(organizationDto);
        return organizationDomainService.updateOrganization(tenantUid, orgUid, org);
    }

    @Override
    public Integer deleteOrganization(String tenantUid, String orgUid) throws FunctionalException {
        return organizationDomainService.deleteOrganization(tenantUid, orgUid);
    }

}
