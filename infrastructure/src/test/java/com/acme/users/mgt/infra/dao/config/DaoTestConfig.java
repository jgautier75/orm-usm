package com.acme.users.mgt.infra.dao.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.acme.users.mgt.infra.dao.api.organizations.IOrganizationsDao;
import com.acme.users.mgt.infra.dao.api.tenants.ITenantsDao;
import com.acme.users.mgt.infra.dao.api.users.IUsersDao;
import com.acme.users.mgt.infra.dao.impl.organizations.OrganizationsDao;
import com.acme.users.mgt.infra.dao.impl.tenants.TenantsDao;
import com.acme.users.mgt.infra.dao.impl.users.UsersDao;

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

}
