package com.acme.users.mgt.infra.services.impl.sectors;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.acme.jga.users.mgt.domain.sectors.v1.Sector;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.users.mgt.infra.converters.SectorsConverter;
import com.acme.users.mgt.infra.dao.api.sectors.ISectorsDao;
import com.acme.users.mgt.infra.dto.sectors.v1.SectorDb;
import com.acme.users.mgt.infra.services.api.sectors.ISectorsInfraService;
import com.acme.users.mgt.logging.services.api.ILogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SectorsInfraService implements ISectorsInfraService {
    private final ISectorsDao sectorsDao;
    private final SectorsConverter sectorsConverter;
    private final ILogService logService;

    @Override
    public Sector fetchSectorsWithHierarchy(Long tenantId, Long organizationId) {
        List<SectorDb> sectors = sectorsDao.findSectorsByOrgId(tenantId, organizationId);
        SectorDb rootSectorDb = null;
        Sector targetSector = null;

        if (!CollectionUtils.isEmpty(sectors)) {
            Optional<SectorDb> optRootSector = sectors.stream().filter(SectorDb::isRoot).findFirst();
            if (optRootSector.isPresent()) {
                rootSectorDb = optRootSector.get();
                mapSectorsResursively(rootSectorDb, sectors);
            }
        }

        if (rootSectorDb != null) {
            targetSector = sectorsConverter.convertSectorDbToDomain(rootSectorDb);
        }

        return targetSector;
    }

    @Override
    public CompositeId createSector(Long tenantId, Long organizationId, Sector sector) {
        SectorDb sectorDb = sectorsConverter.convertSectorDomaintoDb(sector);

        CompositeId compositeId = sectorsDao.createSector(tenantId, organizationId, sectorDb);
        logService.infoS(this.getClass().getName() + "createSector",
                "Created sector with uid [%s] on tenant [%s] and organization [%s]",
                new Object[] { compositeId.getUid(), tenantId, organizationId });

        return compositeId;
    }

    @Override
    public Sector findSectorByUid(Long tenantId, Long orgId, String sectorUid) {
        SectorDb sectorDb = sectorsDao.findByUid(tenantId, orgId, sectorUid);
        return sectorsConverter.convertSectorDbToDomain(sectorDb);
    }

    @Override
    public Optional<Long> existsByCode(String code) {
        return sectorsDao.existsByCode(code);
    }

    /**
     * Build sectors hierarchy.
     * 
     * @param parentSector Parent sector
     * @param sectors      Sectors list
     */
    protected void mapSectorsResursively(SectorDb parentSector, List<SectorDb> sectors) {
        List<SectorDb> children = sectors.stream()
                .filter(sect -> sect.getParentId() != null
                        && sect.getParentId().longValue() == parentSector.getId().longValue())
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(children)) {
            for (SectorDb child : children) {
                parentSector.addChild(child);
                mapSectorsResursively(child, sectors);
            }
        }
    }

    @Override
    public int updateSector(Long tenantId, Long orgId, Sector sector) {
        SectorDb sectorDb = sectorsConverter.convertSectorDomaintoDb(sector);
        return sectorsDao.updateSector(tenantId, orgId, sectorDb);
    }

}
