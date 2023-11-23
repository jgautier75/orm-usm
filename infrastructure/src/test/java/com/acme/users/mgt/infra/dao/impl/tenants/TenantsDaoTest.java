package com.acme.users.mgt.infra.dao.impl.tenants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import com.acme.users.mgt.infra.dao.api.tenants.ITenantsDao;
import com.acme.users.mgt.infra.dao.config.DaoTestConfig;
import com.acme.users.mgt.infra.dao.config.DatabaseTestConfig;
import com.acme.users.mgt.infra.dao.utils.DaoTestUtils;
import com.acme.users.mgt.infra.dto.tenants.v1.TenantDb;

@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = { DatabaseTestConfig.class, DaoTestConfig.class })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Transactional
@ContextConfiguration(initializers = TenantsDaoTest.DataSourceInitializer.class)
class TenantsDaoTest {

    public static class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
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

    @Container
    private static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:16.1")
            .waitingFor(Wait.defaultWaitStrategy());

    @Autowired
    ITenantsDao tenantsDao;

    @BeforeEach
    public void beforeTests() throws Exception {
        DaoTestUtils.performLiquibaseUpdate(database.getJdbcUrl(), database.getUsername(), database.getPassword());
    }

    @Test
    void createTenantTest() throws Exception {

        String tenantCode = "001";

        // Create tenant
        CompositeId compositeId = tenantsDao.createTenant(tenantCode, "root");
        assertNotNull(compositeId, "Composite id not null");
        assertNotNull(compositeId.getId(), "Id from sequence not null");
        assertNotNull(compositeId.getUid(), "Generated uid not null");

        // Find by id
        TenantDb tenantById = tenantsDao.findById(compositeId.getId());
        assertNotNull(tenantById, "Tenant by id not null");
        assertNotNull(tenantById.getCode(), "Tenant by id: code not null");
        assertNotNull(tenantById.getId(), "Tenant by id: id not null");
        assertNotNull(tenantById.getLabel(), "Tenant by id: label not null");
        assertNotNull(tenantById.getUid(), "Tenant by id: uid not null");
        assertEquals(compositeId.getUid(), tenantById.getUid());

        // Find by uid
        TenantDb tenantByUid = tenantsDao.findByUid(compositeId.getUid());
        assertNotNull(tenantByUid, "Tenant by uid not null");

        // Find by code
        TenantDb tenantByCode = tenantsDao.findByCode(tenantCode);
        assertNotNull(tenantByCode, "Tenant by code not null");

        // Check returns null
        TenantDb tenantDummy = tenantsDao.findByCode("123456");
        assertNull("Dummy tenant not found", tenantDummy);

        // Update tenant
        tenantById.setCode("123456");
        tenantById.setLabel("test");
        Integer nbUpdated = tenantsDao.updateTenant(compositeId.getId(), tenantById.getCode(), tenantById.getLabel());
        assertEquals("1 row updated", (Integer) 1, nbUpdated);

        // Delete tenant
        Integer nbDeleted = tenantsDao.deleteTenant(compositeId.getId());
        assertEquals("1 row deleted", (Integer) 1, nbDeleted);

    }

}
