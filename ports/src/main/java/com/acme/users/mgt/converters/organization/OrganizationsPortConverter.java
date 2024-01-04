package com.acme.users.mgt.converters.organization;

import org.springframework.stereotype.Component;

import com.acme.jga.users.mgt.domain.organizations.v1.Organization;
import com.acme.jga.users.mgt.domain.organizations.v1.OrganizationCommons;
import com.acme.users.mgt.dto.port.organizations.v1.OrganizationCommonsDto;
import com.acme.users.mgt.dto.port.organizations.v1.OrganizationDto;
import com.acme.users.mgt.dto.port.organizations.v1.OrganizationLightDto;

@Component
public class OrganizationsPortConverter {

    public Organization convertOrganizationDtoToDomain(OrganizationDto organizationDto) {
        Organization organization = null;
        if (organizationDto != null) {
            organization = new Organization();
            organization.setCommons(convertOrganizationCommonDtoToDomain(organizationDto.getCommons()));
        }
        return organization;
    }

    public OrganizationDto convertOrganizationToDto(Organization org) {
        OrganizationDto dto = null;
        if (org != null) {
            dto = OrganizationDto.builder()
                    .commons(convertOrganizationCommonsToDto(org.getCommons()))
                    .id(org.getId())
                    .uid(org.getUid())
                    .build();
        }
        return dto;
    }

    public OrganizationCommons convertOrganizationCommonDtoToDomain(OrganizationCommonsDto organizationCommonsDto) {
        OrganizationCommons commons = null;
        if (organizationCommonsDto != null) {
            commons = OrganizationCommons.builder()
                    .code(organizationCommonsDto.getCode())
                    .country(organizationCommonsDto.getCountry())
                    .kind(organizationCommonsDto.getKind())
                    .label(organizationCommonsDto.getLabel())
                    .status(organizationCommonsDto.getStatus())
                    .build();
        }
        return commons;
    }

    public OrganizationCommonsDto convertOrganizationCommonsToDto(OrganizationCommons organizationCommons) {
        OrganizationCommonsDto dto = null;
        if (organizationCommons != null) {
            dto = OrganizationCommonsDto.builder()
                    .code(organizationCommons.getCode())
                    .country(organizationCommons.getCountry())
                    .kind(organizationCommons.getKind())
                    .label(organizationCommons.getLabel())
                    .status(organizationCommons.getStatus())
                    .build();
        }
        return dto;
    }

    public OrganizationLightDto convertOrganizationToLightOrgDto(Organization organization) {
        OrganizationLightDto organizationLightDto = null;
        if (organization != null) {
            organizationLightDto = new OrganizationLightDto();
            organizationLightDto.setKind(organization.getCommons().getKind());
            organizationLightDto.setLabel(organization.getCommons().getLabel());
            organizationLightDto.setStatus(organization.getCommons().getStatus());
            organizationLightDto.setUid(organization.getUid());
            organizationLightDto.setCode(organization.getCommons().getCode());
        }
        return organizationLightDto;
    }

}
