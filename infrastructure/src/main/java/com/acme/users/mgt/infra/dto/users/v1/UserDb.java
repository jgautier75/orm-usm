package com.acme.users.mgt.infra.dto.users.v1;

import com.acme.jga.users.mgt.dto.api.ApiVersion;
import com.acme.jga.users.mgt.dto.api.IVersioned;
import com.acme.jga.users.mgt.dto.users.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class UserDb implements IVersioned {
	private Long id;
	private String uid;
	private Long tenantId;
	private Long orgId;
	private String login;
	private String firstName;
	private String lastName;
	private String middleName;
	private String email;
	private UserStatus status;

	@Override
	public ApiVersion getVersion() {
		return ApiVersion.V1;
	}

}
