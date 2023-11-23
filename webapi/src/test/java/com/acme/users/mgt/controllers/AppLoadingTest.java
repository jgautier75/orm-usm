package com.acme.users.mgt.controllers;

import static org.awaitility.Awaitility.*;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import liquibase.command.CommandScope;
import liquibase.command.core.UpdateCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionCommandStep;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = {})
@Slf4j
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Transactional
@ContextConfiguration(initializers = AppLoadingTest.DataSourceInitializer.class)
class AppLoadingTest {
    @LocalServerPort
    int randomServerPort;

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

    @BeforeEach
    void initDb() throws Exception {
        try (Connection conn = DriverManager.getConnection(database.getJdbcUrl(), database.getUsername(),
                database.getPassword())) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(conn));
            CommandScope updateCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME);
            updateCommand.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, database);
            updateCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "postgresql/changelogs.xml");
            updateCommand.execute();
        }
    }

    @Test
    void actuator() throws Exception {
        await().atMost(Duration.ofSeconds(30L))
                .until(actuatorHttpStatus200());
    }

    /**
     * Fetch actuator url.
     * 
     * @return Http status code
     */
    private Callable<Boolean> actuatorHttpStatus200() {
        final AtomicInteger httpStatus = new AtomicInteger();
        CloseableHttpClient httpClient = HttpClientBuilder.create().disableAutomaticRetries().build();
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            String actuatorUrl = "http://" + hostName + ":" + randomServerPort + "/orm-usm/actuator";
            HttpGet httpGet = new HttpGet(actuatorUrl);
            Integer code = httpClient.execute(httpGet,
                    new HttpClientResponseHandler<Integer>() {
                        @Override
                        public Integer handleResponse(ClassicHttpResponse response)
                                throws HttpException, IOException {
                            return response.getCode();
                        }
                    });
            httpStatus.set(code);
            log.info("Http response status:" + httpStatus);
        } catch (Exception e) {
            log.error("Unable to query actuator", e);
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                // Silent catch
            }
        }
        return () -> httpStatus != null && httpStatus.get() == HttpStatus.OK.value();
    }

}
