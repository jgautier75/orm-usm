package com.acme.jga.users.mgt.domain.users.v1;

import java.io.Serializable;

import com.acme.jga.users.mgt.domain.organizations.v1.Organization;
import com.acme.jga.users.mgt.dto.api.ApiVersion;
import com.acme.jga.users.mgt.dto.api.IVersioned;
import com.acme.jga.users.mgt.dto.users.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class User implements Serializable, IVersioned {
	private static final long serialVersionUID = -2560819667784907502L;
	private Long id;
	private String uid;
	private Long tenantId;
	private Long organizationId;
	private UserCredentials credentials;
	private UserCommons commons;
	private UserStatus status;
	private Organization organization;

	@Override
	public ApiVersion getVersion() {
		return ApiVersion.V1;
	}

}
