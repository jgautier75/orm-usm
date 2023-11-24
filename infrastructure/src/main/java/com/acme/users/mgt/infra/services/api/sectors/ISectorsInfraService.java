package com.acme.users.mgt.infra.services.api.sectors;

import com.acme.jga.users.mgt.domain.sectors.v1.Sector;
import com.acme.jga.users.mgt.dto.ids.CompositeId;

public interface ISectorsInfraService {
    Sector fetchSectorsWithHierarchy(Long tenantId, Long organizationId);

    CompositeId createSector(Long tenantId, Long organizationId, Sector sector);

    Sector findSectorByUid(Long tenantId, Long orgId, String sectorUid);
}
