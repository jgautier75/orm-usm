package com.acme.users.mgt.services.sectors.api;

import com.acme.jga.users.mgt.domain.sectors.v1.Sector;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.exceptions.FunctionalException;

public interface ISectorsDomainService {
        CompositeId createSector(String tenantUid, String organizationUid, Sector sector) throws FunctionalException;

        Sector findSectorByUidTenantOrg(String tenantUid, String organizationUid, String sectorUid)
                        throws FunctionalException;

        Sector findSectorByUidTenantOrg(Long tenantId, Long organizationId, String sectorUid)
                        throws FunctionalException;

        Sector fetchSectorsWithHierarchy(String tenantUid, String organizationUid) throws FunctionalException;

}
