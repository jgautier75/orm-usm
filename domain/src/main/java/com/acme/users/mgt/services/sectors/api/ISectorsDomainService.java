package com.acme.users.mgt.services.sectors.api;

import com.acme.jga.users.mgt.domain.sectors.v1.Sector;
import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.exceptions.FunctionalException;

public interface ISectorsDomainService {

        /**
         * Create sector.
         * 
         * @param tenantUid       Tenant uid
         * @param organizationUid Organization uid
         * @param sector          Sector
         * @return Composite id (internal & external)
         * @throws FunctionalException Functional error
         */
        CompositeId createSector(String tenantUid, String organizationUid, Sector sector) throws FunctionalException;

        Sector findSectorByUidTenantOrg(String tenantUid, String organizationUid, String sectorUid)
                        throws FunctionalException;

        /**
         * Find sector.
         * 
         * @param tenantId       Tenant internal id
         * @param organizationId Organization internal id
         * @param sectorUid      Sector external id
         * @return Sector
         * @throws FunctionalException Functional error
         */
        Sector findSectorByUidTenantOrg(Long tenantId, Long organizationId, String sectorUid)
                        throws FunctionalException;

        /**
         * Find sectors hierarchy for an organization.
         * 
         * @param tenantUid       Tenant uid
         * @param organizationUid Organization uid
         * @return Root sector with children (recursive)
         * @throws FunctionalException Functional error
         */
        Sector fetchSectorsWithHierarchy(String tenantUid, String organizationUid) throws FunctionalException;

        /**
         * Update sector.
         * 
         * @param tenantUid       Tenant uid
         * @param organizationUid Organization uid
         * @param sectorUid       Sector uid
         * @param sector          Sector
         * @return Number of rows update
         * @throws FunctionalException Functional error
         */
        Integer updateSector(String tenantUid, String organizationUid, String sectorUid, Sector sector)
                        throws FunctionalException;

        /**
         * Delete sector.
         * 
         * @param tenantUid       Tenant uid
         * @param organizationUid Organization uid
         * @param sectorUid       Sector uid
         * @return Nb of deleted sectors
         * @throws FunctionalException Functional error
         */
        Integer deleteSector(String tenantUid, String organizationUid, String sectorUid) throws FunctionalException;

}
