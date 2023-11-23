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
public class UserCommons implements Serializable, IVersioned {
	private static final long serialVersionUID = 6525190905413054113L;
	private String firstName;
	private String lastName;
	private String middleName;

	@Override
	public ApiVersion getVersion() {
		return ApiVersion.V1;
	}

}
