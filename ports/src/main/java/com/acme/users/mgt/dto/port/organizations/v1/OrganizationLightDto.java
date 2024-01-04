package com.acme.users.mgt.dto.port.organizations.v1;

import java.io.Serializable;

import com.acme.jga.users.mgt.dto.api.ApiVersion;
import com.acme.jga.users.mgt.dto.api.IVersioned;
import com.acme.jga.users.mgt.dto.organizations.OrganizationKind;
import com.acme.jga.users.mgt.dto.organizations.OrganizationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class OrganizationLightDto implements Serializable, IVersioned {
	private static final long serialVersionUID = 1877592832111032724L;
	private String uid;
	private String label;
	private String code;
	private OrganizationKind kind;
	private OrganizationStatus status;

	@Override
	public ApiVersion getVersion() {
		return ApiVersion.V1;
	}

}
