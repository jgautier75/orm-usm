package com.acme.users.mgt.converters.sectors;

import org.springframework.stereotype.Component;

import com.acme.jga.users.mgt.domain.sectors.v1.Sector;
import com.acme.users.mgt.dto.port.sectors.v1.SectorDto;

@Component
public class SectorsPortConverter {

    public Sector convertSectorDtoToDomain(SectorDto sectorDto) {
        Sector targetSector = null;
        if (sectorDto != null) {
            targetSector = Sector.builder()
                    .code(sectorDto.getCode())
                    .label(sectorDto.getLabel())
                    .parentUid(sectorDto.getParentUid())
                    .root(sectorDto.isRoot())
                    .build();
        }
        return targetSector;
    }

}
