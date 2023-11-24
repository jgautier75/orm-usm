package com.acme.users.mgt.infra.dao.api.sectors;

import java.util.List;

import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.users.mgt.infra.dto.sectors.v1.SectorDb;

public interface ISectorsDao {

    SectorDb findByUid(Long tenantId, Long orgId, String uid);

    List<SectorDb> findSectorsByOrgId(Long tenantId, Long orgId);

    CompositeId createSector(Long tenantId, Long orgId, SectorDb sectorDb);
}
