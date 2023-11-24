package com.acme.users.mgt.services.api.sectors;

import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.users.mgt.dto.port.sectors.v1.SectorDto;
import com.acme.users.mgt.dto.port.shared.UidDto;

public interface ISectorsPortService {
    UidDto createSector(String tenantUid, String organizationUid, SectorDto sectorDto) throws FunctionalException;
}
