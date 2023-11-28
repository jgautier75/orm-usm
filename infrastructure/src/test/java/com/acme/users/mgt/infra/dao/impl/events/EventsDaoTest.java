package com.acme.users.mgt.infra.dao.impl.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.UUID;

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

import com.acme.jga.users.mgt.domain.events.v1.AuditAction;
import com.acme.jga.users.mgt.domain.events.v1.AuditAuthor;
import com.acme.jga.users.mgt.domain.events.v1.AuditChange;
import com.acme.jga.users.mgt.domain.events.v1.AuditEvent;
import com.acme.jga.users.mgt.domain.events.v1.AuditScope;
import com.acme.jga.users.mgt.domain.events.v1.EventStatus;
import com.acme.jga.users.mgt.domain.events.v1.EventTarget;
import com.acme.jga.users.mgt.utils.DateTimeUtils;
import com.acme.users.mgt.infra.converters.AuditEventsInfraConverter;
import com.acme.users.mgt.infra.dao.api.events.IEventsDao;
import com.acme.users.mgt.infra.dao.config.DaoTestConfig;
import com.acme.users.mgt.infra.dao.config.DatabaseTestConfig;
import com.acme.users.mgt.infra.dao.utils.DaoTestUtils;
import com.acme.users.mgt.infra.dto.events.v1.AuditEventDb;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = { DatabaseTestConfig.class, DaoTestConfig.class })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Transactional
@ContextConfiguration(initializers = EventsDaoTest.DataSourceInitializer.class)
class EventsDaoTest {

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
        IEventsDao eventsDao;

        @Autowired
        AuditEventsInfraConverter auditEventsInfraConverter;

        @Autowired
        ObjectMapper objectMapper;

        @Container
        private static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:16.1")
                        .waitingFor(Wait.defaultWaitStrategy());

        @BeforeEach
        void initDb() throws Exception {
                DaoTestUtils.performLiquibaseUpdate(database.getJdbcUrl(), database.getUsername(),
                                database.getPassword());
        }

        @Test
        void createEvent() throws Exception {
                AuditAuthor author = AuditAuthor.builder()
                                .name("Jerome GAUTIER")
                                .uid(UUID.randomUUID().toString())
                                .build();
                AuditScope auditScope = AuditScope.builder()
                                .organizationName("RENNES")
                                .organizationUid(UUID.randomUUID().toString())
                                .tenantName("SI")
                                .tenantName(UUID.randomUUID().toString())
                                .build();
                AuditChange auditChange = AuditChange.builder().from("a").object("test").to("b").build();

                AuditEvent auditEvent = AuditEvent.builder()
                                .action(AuditAction.CREATE)
                                .author(author)
                                .changes(List.of(auditChange))
                                .createdAt(DateTimeUtils.nowIso())
                                .lastUpdatedAt(DateTimeUtils.nowIso())
                                .objectUid(UUID.randomUUID().toString())
                                .scope(auditScope)
                                .status(EventStatus.PENDING)
                                .target(EventTarget.SECTOR)
                                .build();
                objectMapper.writeValueAsString(auditEvent);

                AuditEventDb auditEventDb = auditEventsInfraConverter.convertAuditEventToDb(auditEvent, objectMapper);
                String uid = eventsDao.insertEvent(auditEventDb);
                assertNotNull("Generated uid not null", uid);

                AuditEventDb rdbmsAudit = eventsDao.findByUid(uid);
                assertAll("Find by uid",
                                () -> assertNotNull("Audit event not null", rdbmsAudit),
                                () -> assertNotNull("Payload not null", rdbmsAudit.getPayload()),
                                () -> assertEquals("Action match", auditEvent.getAction(), rdbmsAudit.getAction()),
                                () -> assertNotNull("Created at", rdbmsAudit.getCreatedAt()),
                                () -> assertNotNull("Last updated at", rdbmsAudit.getLastUpdatedAt()),
                                () -> assertEquals("Object uid match", auditEvent.getObjectUid(),
                                                rdbmsAudit.getObjectUid()),
                                () -> assertEquals("Status match", auditEvent.getStatus(),
                                                rdbmsAudit.getStatus()),
                                () -> assertEquals("Target match", auditEvent.getTarget(),
                                                rdbmsAudit.getTarget()),
                                () -> assertNotNull("Uid not null", rdbmsAudit.getUid()));
        }
}
