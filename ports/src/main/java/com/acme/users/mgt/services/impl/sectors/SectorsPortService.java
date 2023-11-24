package com.acme.users.mgt.services.impl.sectors;

import org.springframework.stereotype.Service;

import com.acme.jga.users.mgt.domain.sectors.v1.Sector;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.users.mgt.converters.sectors.SectorsPortConverter;
import com.acme.users.mgt.dto.port.sectors.v1.SectorDto;
import com.acme.users.mgt.dto.port.shared.UidDto;
import com.acme.users.mgt.services.api.sectors.ISectorsPortService;
import com.acme.users.mgt.services.sectors.api.ISectorsDomainService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SectorsPortService implements ISectorsPortService {
    private final ISectorsDomainService sectorsDomainService;
    private final SectorsPortConverter sectorsConverter;

    @Override
    public UidDto createSector(String tenantUid, String organizationUid, SectorDto sectorDto)
            throws FunctionalException {
        Sector sector = sectorsConverter.convertSectorDtoToDomain(sectorDto);
        CompositeId compositeId = sectorsDomainService.createSector(tenantUid, organizationUid, sector);
        return UidDto.builder().uid(compositeId.getUid()).build();
    }

}
