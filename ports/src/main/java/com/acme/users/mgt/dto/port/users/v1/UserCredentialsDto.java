package com.acme.users.mgt.dto.port.users.v1;

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
public class UserCredentialsDto implements Serializable, IVersioned {
	private static final long serialVersionUID = -7399495930708950049L;
	private String login;
	private String email;

	@Override
	public ApiVersion getVersion() {
		return ApiVersion.V1;
	}
}
