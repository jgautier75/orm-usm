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
public class OrganizationCommonsDto implements Serializable, IVersioned {
	private static final long serialVersionUID = 2655548706344486737L;
	private String code;
	private String label;
	private OrganizationKind kind;
	private OrganizationStatus status;
	private String country;

	@Override
	public ApiVersion getVersion() {
		return ApiVersion.V1;
	}
}
