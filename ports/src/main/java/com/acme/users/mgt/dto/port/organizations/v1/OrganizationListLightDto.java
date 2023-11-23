package com.acme.users.mgt.dto.port.organizations.v1;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class OrganizationListLightDto {
    private List<OrganizationLightDto> organizations;
}
