package com.acme.users.mgt.infra.dao.impl.users;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
import com.acme.jga.users.mgt.dto.users.UserStatus;
import com.acme.users.mgt.infra.dao.api.organizations.IOrganizationsDao;
import com.acme.users.mgt.infra.dao.api.tenants.ITenantsDao;
import com.acme.users.mgt.infra.dao.api.users.IUsersDao;
import com.acme.users.mgt.infra.dao.config.DaoTestConfig;
import com.acme.users.mgt.infra.dao.config.DatabaseTestConfig;
import com.acme.users.mgt.infra.dao.utils.DaoTestUtils;
import com.acme.users.mgt.infra.dto.organizations.v1.OrganizationDb;
import com.acme.users.mgt.infra.dto.users.v1.UserDb;

@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = { DatabaseTestConfig.class, DaoTestConfig.class })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Transactional
@ContextConfiguration(initializers = UsersDaoTest.DataSourceInitializer.class)
class UsersDaoTest {

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

        @Container
        private static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:16.1")
                        .waitingFor(Wait.defaultWaitStrategy());

        @Autowired
        ITenantsDao tenantsDao;
        @Autowired
        IOrganizationsDao organizationsDao;
        @Autowired
        IUsersDao usersDao;

        @BeforeEach
        void initDb() throws Exception {
                DaoTestUtils.performLiquibaseUpdate(database.getJdbcUrl(), database.getUsername(),
                                database.getPassword());
        }

        @Test
        void createUser() {

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
                assertNotNull("Organization composite id not null", orgCompositeId);
                assertNotNull("Organization id not null", orgCompositeId.getId());
                assertNotNull("Organization uid not null", orgCompositeId.getUid());

                // Create user
                UserDb userDb = UserDb.builder()
                                .email("test@test.fr")
                                .firstName("ftest")
                                .lastName("lname")
                                .login("login_test")
                                .middleName("jr")
                                .orgId(orgCompositeId.getId())
                                .status(UserStatus.ACTIVE)
                                .tenantId(tenantCompositeId.getId())
                                .build();

                CompositeId userCompositeId = usersDao.createUser(userDb);
                assertNotNull("User create: compositeId not null", userCompositeId);

                // Find by id
                UserDb userRdbms = usersDao.findById(tenantCompositeId.getId(), orgCompositeId.getId(),
                                userCompositeId.getId());
                assertNotNull("User find by id: user not null", userRdbms);
                assertEquals("User find by id: id match", userCompositeId.getId(), userRdbms.getId());
                assertEquals("User find by id: email match", (String) userDb.getEmail(), (String) userRdbms.getEmail());
                assertEquals("User find by id: firstName match", userDb.getFirstName(), userRdbms.getFirstName());
                assertEquals("User find by id: lastName match", userDb.getLastName(), userRdbms.getLastName());
                assertEquals("User find by id: login match", userDb.getLogin(), userRdbms.getLogin());
                assertEquals("User find by id: middleName match", userDb.getMiddleName(), userRdbms.getMiddleName());
                assertEquals("User find by id: orgId match", orgCompositeId.getId(), userRdbms.getOrgId());
                assertEquals("User find by id: orgId match", userDb.getStatus(), userRdbms.getStatus());
                assertEquals("User find by id: tenantId match", tenantCompositeId.getId(), userRdbms.getTenantId());
                assertEquals("User find by id: uid match", userCompositeId.getUid(), userRdbms.getUid());

                // Find user by uid
                UserDb userByUid = usersDao.findByUid(tenantCompositeId.getId(), orgCompositeId.getId(),
                                userCompositeId.getUid());
                assertNotNull("User by uid: not null", userByUid);

                // Update user
                userByUid.setEmail("titi.toto@test.fr");
                userByUid.setFirstName("titi");
                userByUid.setLastName("toto");
                userByUid.setMiddleName("md");
                userByUid.setLogin("tutu");
                userByUid.setStatus(UserStatus.INACTIVE);
                Integer nbUpdated = usersDao.updateUser(userByUid);
                assertEquals("1 row updated", (Integer) 1, nbUpdated);

                UserDb updatedUser = usersDao.findById(tenantCompositeId.getId(), orgCompositeId.getId(),
                                userCompositeId.getId());
                assertEquals("Updated user: email match", (String) userByUid.getEmail(),
                                (String) updatedUser.getEmail());
                assertEquals("Updated user: first name match", (String) userByUid.getFirstName(),
                                (String) updatedUser.getFirstName());
                assertEquals("Updated user: last name match", (String) userByUid.getLastName(),
                                (String) updatedUser.getLastName());
                assertEquals("Updated user: middle name match", (String) userByUid.getMiddleName(),
                                (String) updatedUser.getMiddleName());
                assertEquals("Updated user: login match", (String) userByUid.getLogin(),
                                (String) updatedUser.getLogin());
                assertEquals("Updated user: status match", (Integer) userByUid.getStatus().getCode(),
                                (Integer) updatedUser.getStatus().getCode());

                // Delete user
                Integer nbDeleted = usersDao.deleteUser(tenantCompositeId.getId(), orgCompositeId.getId(),
                                userCompositeId.getId());
                assertEquals("1 row deleted", (Integer) 1, nbDeleted);

                UserDb userDeleted = usersDao.findById(tenantCompositeId.getId(), orgCompositeId.getId(),
                                userCompositeId.getId());
                assertNull("Deleted user not found", userDeleted);
        }

}
