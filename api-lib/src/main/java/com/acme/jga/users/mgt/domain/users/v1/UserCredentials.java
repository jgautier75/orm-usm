package com.acme.jga.users.mgt.domain.users.v1;

import java.io.Serializable;

import com.acme.jga.users.mgt.dto.api.ApiVersion;
import com.acme.jga.users.mgt.dto.api.IVersioned;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class UserCredentials implements Serializable, IVersioned {
	private static final long serialVersionUID = 8824656658346637238L;
	private String login;
	private String email;

	@Override
	public ApiVersion getVersion() {
		return ApiVersion.V1;
	}

}
