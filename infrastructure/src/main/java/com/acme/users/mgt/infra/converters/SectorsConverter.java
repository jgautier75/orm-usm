package com.acme.users.mgt.infra.converters;

import org.springframework.stereotype.Component;

import com.acme.jga.users.mgt.domain.sectors.v1.Sector;
import com.acme.users.mgt.infra.dto.sectors.v1.SectorDb;

@Component
public class SectorsConverter {

    public Sector convertSectorDbToDomain(SectorDb sectorDb) {
        Sector sector = null;
        if (sectorDb != null) {
            sector = Sector.builder()
                    .code(sectorDb.getCode())
                    .id(sectorDb.getId())
                    .label(sectorDb.getLabel())
                    .orgId(sectorDb.getOrgId())
                    .root(sectorDb.isRoot())
                    .uid(sectorDb.getUid())
                    .build();
            if (sectorDb.hasChildren()) {
                for (SectorDb child : sectorDb.getChildren()) {
                    Sector childSector = convertSectorDbToDomain(child);
                    if (childSector != null) {
                        sector.addChild(childSector);
                    }
                }

            }
        }
        return sector;
    }

    public SectorDb convertSectorDomaintoDb(Sector sector) {
        SectorDb sectorDb = null;
        if (sector != null) {
            sectorDb = SectorDb.builder()
                    .code(sector.getCode())
                    .id(sector.getId())
                    .label(sector.getLabel())
                    .orgId(sector.getOrgId())
                    .parentId(sector.getParentId())
                    .root(sector.isRoot())
                    .tenantId(sector.getTenantId())
                    .uid(sector.getUid())
                    .build();
        }
        return sectorDb;
    }

}
