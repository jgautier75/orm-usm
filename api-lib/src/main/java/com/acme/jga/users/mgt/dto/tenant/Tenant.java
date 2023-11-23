package com.acme.jga.users.mgt.dto.tenant;

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
public class Tenant implements IVersioned {
	private Long id;
	private String uid;
	private String code;
	private String label;

	@Override
	public ApiVersion getVersion() {
		return ApiVersion.V1;
	}

}
