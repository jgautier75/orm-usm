package com.acme.users.mgt.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.users.mgt.dto.port.sectors.v1.SectorDisplayDto;
import com.acme.users.mgt.dto.port.sectors.v1.SectorDto;
import com.acme.users.mgt.dto.port.shared.UidDto;
import com.acme.users.mgt.services.api.sectors.ISectorsPortService;
import com.acme.users.mgt.versioning.WebApiVersions.SectorsResourceVersion;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SectorsController {
    private final ISectorsPortService sectorsPortService;

    @PostMapping(value = SectorsResourceVersion.ROOT)
    public ResponseEntity<UidDto> createSector(@PathVariable("tenantUid") String tenantUid,
            @PathVariable("orgUid") String orgUid, @RequestBody SectorDto sectorDto) throws FunctionalException {
        UidDto uidDto = sectorsPortService.createSector(tenantUid, orgUid, sectorDto);
        return new ResponseEntity<>(uidDto, HttpStatus.CREATED);
    }

    @GetMapping(value = SectorsResourceVersion.ROOT)
    public ResponseEntity<SectorDisplayDto> findSectors(@PathVariable("tenantUid") String tenantUid,
            @PathVariable("orgUid") String orgUid) throws FunctionalException {
        SectorDisplayDto sectorDisplayDto = sectorsPortService.findSectors(tenantUid, orgUid);
        return new ResponseEntity<>(sectorDisplayDto, HttpStatus.OK);
    }

    @PutMapping(value = SectorsResourceVersion.WITH_UID)
    public ResponseEntity<Void> updateSector(@PathVariable("tenantUid") String tenantUid,
            @PathVariable("orgUid") String orgUid,
            @PathVariable("sectorUid") String sectorUid,
            @RequestBody SectorDto sector) throws FunctionalException {
        sectorsPortService.updateSector(tenantUid, orgUid, sectorUid, sector);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = SectorsResourceVersion.WITH_UID)
    public ResponseEntity<Void> deleteSector(@PathVariable("tenantUid") String tenantUid,
            @PathVariable("orgUid") String orgUid,
            @PathVariable("sectorUid") String sectorUid) throws FunctionalException {
        sectorsPortService.deleteSector(tenantUid, orgUid, sectorUid);
        return ResponseEntity.noContent().build();
    }

}
