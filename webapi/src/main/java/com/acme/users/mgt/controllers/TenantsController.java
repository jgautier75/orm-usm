package com.acme.users.mgt.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.users.mgt.dto.port.shared.UidDto;
import com.acme.users.mgt.dto.port.tenants.v1.TenantDisplayDto;
import com.acme.users.mgt.dto.port.tenants.v1.TenantDto;
import com.acme.users.mgt.dto.port.tenants.v1.TenantListDisplayDto;
import com.acme.users.mgt.services.api.tenant.ITenantPortService;
import com.acme.users.mgt.versioning.WebApiVersions.TenantsResourceVersion;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TenantsController {
    private final ITenantPortService tenantPortService;

    @PostMapping(value = TenantsResourceVersion.ROOT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<Object> createTenant(@RequestBody TenantDto tenantDto) throws FunctionalException {
        UidDto uid = tenantPortService.createTenant(tenantDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(uid);
    }

    @GetMapping(value = TenantsResourceVersion.WITH_UID)
    public ResponseEntity<Object> findTenantByUid(@PathVariable(name = "uid", required = true) String uid)
            throws FunctionalException {
        TenantDisplayDto tenantDisplayDto = tenantPortService.findTenantByUid(uid);
        return new ResponseEntity<>(tenantDisplayDto, HttpStatus.OK);
    }

    @GetMapping(value = TenantsResourceVersion.ROOT)
    public ResponseEntity<Object> listTenants() {
        TenantListDisplayDto tenantListDisplayDto = tenantPortService.findAllTenants();
        return new ResponseEntity<>(tenantListDisplayDto, HttpStatus.OK);
    }

    @PostMapping(value = TenantsResourceVersion.WITH_UID)
    public ResponseEntity<Object> updateTenantByUid(@PathVariable(name = "uid", required = true) String uid,
            @RequestBody TenantDto tenantDto)
            throws FunctionalException {
        tenantPortService.updateTenant(uid, tenantDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(value = TenantsResourceVersion.WITH_UID)
    public ResponseEntity<Object> deleteTenantByUid(@PathVariable(name = "uid", required = true) String uid)
            throws FunctionalException {
        tenantPortService.deleteTenant(uid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
