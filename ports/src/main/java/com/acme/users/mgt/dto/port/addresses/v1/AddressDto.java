package com.acme.users.mgt.dto.port.addresses.v1;

import java.io.Serializable;

import com.acme.jga.users.mgt.dto.api.ApiVersion;
import com.acme.jga.users.mgt.dto.api.IVersioned;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AddressDto implements Serializable, IVersioned {
	private static final long serialVersionUID = -5221525673340990769L;
	private Long id;
	private String uid;
	private String street;
	private String number;
	private String zipCode;
	private String state;
	private String country;

	@Override
	public ApiVersion getVersion() {
		return ApiVersion.V1;
	}
}
