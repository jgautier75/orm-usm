package com.acme.users.mgt.infra.services.api.sectors;

import java.util.Optional;

import com.acme.jga.users.mgt.domain.sectors.v1.Sector;
import com.acme.jga.users.mgt.dto.ids.CompositeId;

public interface ISectorsInfraService {
    Sector fetchSectorsWithHierarchy(Long tenantId, Long organizationId);

    CompositeId createSector(Long tenantId, Long organizationId, Sector sector);

    Sector findSectorByUid(Long tenantId, Long orgId, String sectorUid);

    Optional<Long> existsByCode(String code);

    int updateSector(Long tenantId, Long orgId, Sector sector);

    int deleteSector(Long tenantId, Long organizationId, Long sectorId);
}
