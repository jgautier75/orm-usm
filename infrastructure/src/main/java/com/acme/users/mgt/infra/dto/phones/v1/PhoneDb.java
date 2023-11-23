package com.acme.users.mgt.infra.dto.phones.v1;

import com.acme.jga.users.mgt.dto.api.ApiVersion;
import com.acme.jga.users.mgt.dto.api.IVersioned;
import com.acme.jga.users.mgt.dto.phones.PhoneKind;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PhoneDb implements IVersioned {
	private Long id;
	private String uid;
	private PhoneKind kind;
	private String number;

	@Override
	public ApiVersion getVersion() {
		return ApiVersion.V1;
	}
}
