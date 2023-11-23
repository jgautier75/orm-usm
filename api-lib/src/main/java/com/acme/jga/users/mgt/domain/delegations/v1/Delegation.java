package com.acme.jga.users.mgt.domain.delegations.v1;

import java.io.Serializable;

import com.acme.jga.users.mgt.dto.api.ApiVersion;
import com.acme.jga.users.mgt.dto.api.IVersioned;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Delegation implements IVersioned, Serializable {
	private static final long serialVersionUID = 6798278935205070649L;
	private Long delegator;
	private Long delegate;
	private boolean active;

	@Override
	public ApiVersion getVersion() {
		return ApiVersion.V1;
	}

}
