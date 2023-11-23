package com.acme.users.mgt.dto.port.users.v1;

import java.io.Serializable;

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
public class UserDto implements Serializable, IVersioned {
	private static final long serialVersionUID = -1827189801869898632L;
	private Long id;
	private String uid;
	private UserCredentialsDto credentials;
	private UserCommonsDto commons;
	private UserStatus status;

	@Override
	public ApiVersion getVersion() {
		return ApiVersion.V1;
	}
}
