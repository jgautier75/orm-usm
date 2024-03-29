package com.acme.users.mgt.infra.dao.impl.sectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.acme.jga.users.mgt.dto.ids.CompositeId;
import com.acme.jga.users.mgt.dto.organizations.OrganizationKind;
import com.acme.jga.users.mgt.dto.organizations.OrganizationStatus;
import com.acme.users.mgt.infra.dao.api.organizations.IOrganizationsDao;
import com.acme.users.mgt.infra.dao.api.sectors.ISectorsDao;
import com.acme.users.mgt.infra.dao.api.tenants.ITenantsDao;
import com.acme.users.mgt.infra.dao.config.DaoTestConfig;
import com.acme.users.mgt.infra.dao.config.DatabaseTestConfig;
import com.acme.users.mgt.infra.dao.utils.DaoTestUtils;
import com.acme.users.mgt.infra.dto.organizations.v1.OrganizationDb;
import com.acme.users.mgt.infra.dto.sectors.v1.SectorDb;

@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = { DatabaseTestConfig.class, DaoTestConfig.class })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Transactional
@ContextConfiguration(initializers = SectorsDaoTest.DataSourceInitializer.class)
class SectorsDaoTest {

        @Container
        private static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:16.1")
                        .waitingFor(Wait.defaultWaitStrategy());

        public static class DataSourceInitializer
                        implements ApplicationContextInitializer<ConfigurableApplicationContext> {
                @Override
                public void initialize(ConfigurableApplicationContext applicationContext) {
                        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                                        applicationContext,
                                        "spring.test.database.replace=none",
                                        "spring.datasource.url=" + database.getJdbcUrl(),
                                        "spring.datasource.username=" + database.getUsername(),
                                        "spring.datasource.password=" + database.getPassword());
                }
        }

        @Autowired
        ISectorsDao sectorsDao;

        @Autowired
        ITenantsDao tenantsDao;

        @Autowired
        IOrganizationsDao organizationsDao;

        @BeforeEach
        void initDb() throws Exception {
                DaoTestUtils.performLiquibaseUpdate(database.getJdbcUrl(), database.getUsername(),
                                database.getPassword());
        }

        @Test
        void createSector() {

                // Create tenant
                CompositeId tenantCompositeId = tenantsDao.createTenant("001", "root");

                // Create organization
                OrganizationDb organizationDb = OrganizationDb.builder()
                                .country("fr")
                                .kind(OrganizationKind.TENANT)
                                .label("test")
                                .status(OrganizationStatus.ACTIVE)
                                .code("002")
                                .tenantId(tenantCompositeId.getId())
                                .build();
                CompositeId orgCompositeId = organizationsDao.createOrganization(organizationDb);

                SectorDb sectorDb = SectorDb.builder().code("ctest").label("ctest").root(true)
                                .tenantId(tenantCompositeId.getId()).build();

                // Create sector
                CompositeId sectorCompositeId = sectorsDao.createSector(tenantCompositeId.getId(),
                                orgCompositeId.getId(),
                                sectorDb);
                assertAll(() -> assertNotNull("CompositeId not null", sectorCompositeId),
                                () -> assertNotNull("Id not null", sectorCompositeId.getId()),
                                () -> assertNotNull("Uid not null", sectorCompositeId.getUid()));

                // Exists by code
                Optional<Long> optSectorId = sectorsDao.existsByCode(sectorDb.getCode());
                assertAll("Exists by code",
                                () -> assertTrue("Sector exists", optSectorId.isPresent()));

                SectorDb rdbmsSector = sectorsDao.findByUid(tenantCompositeId.getId(), orgCompositeId.getId(),
                                sectorCompositeId.getUid());
                assertAll("Sector by uid",
                                () -> assertNotNull("RDBMS sector not null", sectorDb),
                                () -> assertEquals("Code match", sectorDb.getCode(), rdbmsSector.getCode()),
                                () -> assertEquals("Label match", sectorDb.getLabel(), rdbmsSector.getLabel()),
                                () -> assertEquals("Root flag match", sectorDb.isRoot(), rdbmsSector.isRoot()),
                                () -> assertEquals("Tenant id match", tenantCompositeId.getId(), rdbmsSector.getId()));

                // Find sectors list
                List<SectorDb> sectors = sectorsDao.findSectorsByOrgId(tenantCompositeId.getId(),
                                orgCompositeId.getId());
                assertAll("Sectors",
                                () -> assertNotNull("Sectors list", sectors),
                                () -> assertEquals("1 sector in list", 1, sectors.size()),
                                () -> assertEquals("Sector id match", sectorCompositeId.getId(),
                                                sectors.get(0).getId()));
        }

}
