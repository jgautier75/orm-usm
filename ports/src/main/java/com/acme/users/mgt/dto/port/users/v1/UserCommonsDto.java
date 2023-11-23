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
public class UserCommonsDto implements Serializable, IVersioned {
	private static final long serialVersionUID = 6536062346106161321L;
	private String firstName;
	private String lastName;
	private String middleName;

	@Override
	public ApiVersion getVersion() {
		return ApiVersion.V1;
	}
}
