package com.acme.jga.users.mgt.domain.phones.v1;

import java.io.Serializable;

import com.acme.jga.users.mgt.dto.api.ApiVersion;
import com.acme.jga.users.mgt.dto.api.IVersioned;
import com.acme.jga.users.mgt.dto.phones.PhoneKind;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Phone implements Serializable, IVersioned {
	private static final long serialVersionUID = -8708080446643207787L;
	private Long id;
	private String uid;
	private PhoneKind kind;
	private String number;

	@Override
	public ApiVersion getVersion() {
		return ApiVersion.V1;
	}
}
