package com.acme.users.mgt.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.users.mgt.annotations.MetricPoint;
import com.acme.users.mgt.dto.port.organizations.v1.OrganizationDto;
import com.acme.users.mgt.dto.port.organizations.v1.OrganizationListLightDto;
import com.acme.users.mgt.dto.port.shared.UidDto;
import com.acme.users.mgt.services.api.organization.IOrganizationPortService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrganizationsController {
    private final IOrganizationPortService organizationPortService;

    @MetricPoint(alias = "ORG_CREATE", version = "v1", regex = "/api/v1/tenants/(.*)/organizations")
    public ResponseEntity<Object> createOrganization(@PathVariable("tenantUid") String tenantUid,
            @RequestBody OrganizationDto organizationDto) throws FunctionalException {
        UidDto uidDto = organizationPortService.createOrganization(tenantUid, organizationDto);
        return new ResponseEntity<>(uidDto, HttpStatus.CREATED);
    }

    @GetMapping(value = "/api/v1/tenants/{tenantUid}/organizations")
    public ResponseEntity<Object> findOrgsByTenant(@PathVariable("tenantUid") String tenantUid)
            throws FunctionalException {
        OrganizationListLightDto lightList = organizationPortService.findAllOrgsLightByTenant(tenantUid);
        return new ResponseEntity<>(lightList, HttpStatus.OK);
    }

    @GetMapping(value = "/api/v1/tenants/{tenantUid}/organizations/{orgUid}")
    public ResponseEntity<Object> findOrgDetails(@PathVariable("tenantUid") String tenantUid,
            @PathVariable("orgUid") String orgUid)
            throws FunctionalException {
        OrganizationDto orgDto = organizationPortService.findOrganizationByUid(tenantUid, orgUid);
        return new ResponseEntity<>(orgDto, HttpStatus.OK);
    }

    @PostMapping(value = "/api/v1/tenants/{tenantUid}/organizations/{orgUid}")
    public ResponseEntity<Object> updateOrganization(@PathVariable("tenantUid") String tenantUid,
            @PathVariable("orgUid") String orgUid, @RequestBody OrganizationDto organizationDto)
            throws FunctionalException {
        organizationPortService.updateOrganization(tenantUid, orgUid, organizationDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
