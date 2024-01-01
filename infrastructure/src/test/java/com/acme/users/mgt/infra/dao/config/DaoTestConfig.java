package com.acme.users.mgt.infra.dao.config;

import java.time.ZoneOffset;
import java.util.TimeZone;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.acme.users.mgt.infra.converters.AuditEventsInfraConverter;
import com.acme.users.mgt.infra.dao.api.events.IEventsDao;
import com.acme.users.mgt.infra.dao.api.organizations.IOrganizationsDao;
import com.acme.users.mgt.infra.dao.api.sectors.ISectorsDao;
import com.acme.users.mgt.infra.dao.api.tenants.ITenantsDao;
import com.acme.users.mgt.infra.dao.api.users.IUsersDao;
import com.acme.users.mgt.infra.dao.impl.events.EventsDao;
import com.acme.users.mgt.infra.dao.impl.organizations.OrganizationsDao;
import com.acme.users.mgt.infra.dao.impl.sectors.SectorsDao;
import com.acme.users.mgt.infra.dao.impl.tenants.TenantsDao;
import com.acme.users.mgt.infra.dao.impl.users.UsersDao;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class DaoTestConfig {

	@Bean
	public ITenantsDao tenantsDao(@Autowired DataSource dataSource,
			@Autowired NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		return new TenantsDao(dataSource, namedParameterJdbcTemplate);
	}

	@Bean
	public IOrganizationsDao organizationsDao(@Autowired DataSource dataSource,
			@Autowired NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		return new OrganizationsDao(dataSource, namedParameterJdbcTemplate);
	}

	@Bean
	public IUsersDao usersDao(@Autowired DataSource dataSource,
			@Autowired NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		return new UsersDao(dataSource, namedParameterJdbcTemplate);
	}

	@Bean
	public ISectorsDao sectorsDao(@Autowired DataSource dataSource,
			@Autowired NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		return new SectorsDao(dataSource, namedParameterJdbcTemplate);
	}

	@Bean
	public AuditEventsInfraConverter auditEventsInfraConverter() {
		return new AuditEventsInfraConverter();
	}

	@Bean
	public IEventsDao eventsDao(@Autowired DataSource dataSource,
			@Autowired NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		return new EventsDao(dataSource, namedParameterJdbcTemplate);
	}

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule())
				.configure(SerializationFeature.INDENT_OUTPUT, false);
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
		return objectMapper;
	}
}
