package com.acme.users.mgt.dto.port.delegations.v1;

import java.io.Serializable;

import com.acme.jga.users.mgt.dto.api.ApiVersion;
import com.acme.jga.users.mgt.dto.api.IVersioned;
import com.acme.users.mgt.dto.port.organizations.v1.OrganizationLightDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class DelegationDto implements Serializable, IVersioned {
	private static final long serialVersionUID = 6798278935205070649L;
	private OrganizationLightDto delegator;
	private OrganizationLightDto delegate;
	private boolean active;

	@Override
	public ApiVersion getVersion() {
		return ApiVersion.V1;
	}

}
