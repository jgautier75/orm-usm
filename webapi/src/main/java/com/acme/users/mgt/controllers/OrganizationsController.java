package com.acme.users.mgt.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.users.mgt.dto.port.organizations.v1.OrganizationDto;
import com.acme.users.mgt.dto.port.organizations.v1.OrganizationListLightDto;
import com.acme.users.mgt.dto.port.shared.UidDto;
import com.acme.users.mgt.services.api.organization.IOrganizationPortService;
import com.acme.users.mgt.versioning.WebApiVersions.OrganizationsResourceVersion;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrganizationsController {
    private final IOrganizationPortService organizationPortService;

    @PostMapping(value = OrganizationsResourceVersion.ROOT)
    public ResponseEntity<Object> createOrganization(@PathVariable("tenantUid") String tenantUid,
            @RequestBody OrganizationDto organizationDto) throws FunctionalException {
        UidDto uidDto = organizationPortService.createOrganization(tenantUid, organizationDto);
        return new ResponseEntity<>(uidDto, HttpStatus.CREATED);
    }

    @GetMapping(value = OrganizationsResourceVersion.ROOT)
    public ResponseEntity<Object> findOrgsByTenant(@PathVariable("tenantUid") String tenantUid)
            throws FunctionalException {
        OrganizationListLightDto lightList = organizationPortService.findAllOrgsLightByTenant(tenantUid);
        return new ResponseEntity<>(lightList, HttpStatus.OK);
    }

    @GetMapping(value = OrganizationsResourceVersion.WITH_UID)
    public ResponseEntity<Object> findOrgDetails(@PathVariable("tenantUid") String tenantUid,
            @PathVariable("orgUid") String orgUid)
            throws FunctionalException {
        OrganizationDto orgDto = organizationPortService.findOrganizationByUid(tenantUid, orgUid);
        return new ResponseEntity<>(orgDto, HttpStatus.OK);
    }

    @PostMapping(value = OrganizationsResourceVersion.WITH_UID)
    public ResponseEntity<Object> updateOrganization(@PathVariable("tenantUid") String tenantUid,
            @PathVariable("orgUid") String orgUid, @RequestBody OrganizationDto organizationDto)
            throws FunctionalException {
        organizationPortService.updateOrganization(tenantUid, orgUid, organizationDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = OrganizationsResourceVersion.WITH_UID)
    public ResponseEntity<Object> deleteOrganization(@PathVariable("tenantUid") String tenantUid,
            @PathVariable("orgUid") String orgUid)
            throws FunctionalException {
        organizationPortService.deleteOrganization(tenantUid, orgUid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
