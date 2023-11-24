package com.acme.users.mgt.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.users.mgt.dto.port.sectors.v1.SectorDto;
import com.acme.users.mgt.dto.port.shared.UidDto;
import com.acme.users.mgt.services.api.sectors.ISectorsPortService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SectorsController {
    private final ISectorsPortService sectorsPortService;

    @PostMapping(value = "/api/v1/tenants/{tenantUid}/organizations/{orgUid}/sectors")
    public ResponseEntity<Object> createSector(@PathVariable("tenantUid") String tenantUid,
            @PathVariable("orgUid") String orgUid, @RequestBody SectorDto sectorDto) throws FunctionalException {
        UidDto uidDto = sectorsPortService.createSector(tenantUid, orgUid, sectorDto);
        return new ResponseEntity<>(uidDto, HttpStatus.CREATED);
    }
}
