package com.acme.jga.users.mgt.domain.organizations.v1;

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
public class OrganizationCommons implements IVersioned, Serializable {
	private static final long serialVersionUID = 6339219514949636784L;
	private String code;
	private String label;
	private OrganizationKind kind;
	private String country;
	private OrganizationStatus status;

	@Override
	public ApiVersion getVersion() {
		return ApiVersion.V1;
	}
}
