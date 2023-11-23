package com.acme.users.mgt.infra.dto.organizations.v1;

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
public class OrganizationDb implements IVersioned {
	private Long tenantId;
	private Long id;
	private String code;
	private String uid;
	private String label;
	private OrganizationKind kind;
	private String country;
	private OrganizationStatus status;

	@Override
	public ApiVersion getVersion() {
		return ApiVersion.V1;
	}
}
