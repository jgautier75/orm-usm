package com.acme.users.mgt.services.impl.sectors;

import org.springframework.stereotype.Service;

import com.acme.jga.users.mgt.domain.sectors.v1.Sector;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.users.mgt.converters.sectors.SectorsPortConverter;
import com.acme.users.mgt.dto.port.sectors.v1.SectorDisplayDto;
import com.acme.users.mgt.dto.port.sectors.v1.SectorDto;
import com.acme.users.mgt.dto.port.shared.UidDto;
import com.acme.users.mgt.services.api.sectors.ISectorsPortService;
import com.acme.users.mgt.services.sectors.api.ISectorsDomainService;
import com.acme.users.mgt.validation.ValidationException;
import com.acme.users.mgt.validation.ValidationResult;
import com.acme.users.mgt.validation.sectors.SectorsValidationEngine;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SectorsPortService implements ISectorsPortService {
    private final ISectorsDomainService sectorsDomainService;
    private final SectorsPortConverter sectorsConverter;
    private final SectorsValidationEngine sectorsValidationEngine;

    @Override
    public UidDto createSector(String tenantUid, String organizationUid, SectorDto sectorDto)
            throws FunctionalException {
        ValidationResult validationResult = sectorsValidationEngine.validate(sectorDto);
        if (!validationResult.isSuccess()) {
            throw new ValidationException(validationResult.getErrors());
        }
        Sector sector = sectorsConverter.convertSectorDtoToDomain(sectorDto);
        CompositeId compositeId = sectorsDomainService.createSector(tenantUid, organizationUid, sector);
        return UidDto.builder().uid(compositeId.getUid()).build();
    }

    @Override
    public SectorDisplayDto findSectors(String tenantUid, String organizationUid) throws FunctionalException {
        Sector rootSector = sectorsDomainService.fetchSectorsWithHierarchy(tenantUid, organizationUid);
        return sectorsConverter.convertSectorDomainToSectorDisplay(rootSector);
    }

    @Override
    public Integer updateSector(String tenantUid, String organizationUid, String sectorUid, SectorDto sectorDto)
            throws FunctionalException {
        ValidationResult validationResult = sectorsValidationEngine.validate(sectorDto);
        if (!validationResult.isSuccess()) {
            throw new ValidationException(validationResult.getErrors());
        }
        Sector sector = sectorsConverter.convertSectorDtoToDomain(sectorDto);
        return sectorsDomainService.updateSector(tenantUid, organizationUid, sectorUid, sector);
    }

    @Override
    public Integer deleteSector(String tenantUid, String organizationUid, String sectorUid) throws FunctionalException {
        return sectorsDomainService.deleteSector(tenantUid, organizationUid, sectorUid);
    }

}
