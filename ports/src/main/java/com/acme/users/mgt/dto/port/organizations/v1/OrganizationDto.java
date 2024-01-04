package com.acme.users.mgt.dto.port.organizations.v1;

import java.io.Serializable;
import java.util.List;

import com.acme.jga.users.mgt.dto.api.ApiVersion;
import com.acme.jga.users.mgt.dto.api.IVersioned;
import com.acme.users.mgt.dto.port.addresses.v1.AddressDto;
import com.acme.users.mgt.dto.port.delegations.v1.DelegationDto;
import com.acme.users.mgt.dto.port.phones.v1.PhoneDto;
import com.acme.users.mgt.dto.port.sectors.v1.SectorDisplayDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class OrganizationDto implements Serializable, IVersioned {
	private static final long serialVersionUID = -9068431715296608638L;
	private Long id;
	private String uid;
	private String tenantUid;
	private OrganizationCommonsDto commons;
	private List<PhoneDto> phones;
	private List<AddressDto> addresses;
	private List<DelegationDto> delegations;
	private SectorDisplayDto sector;

	@Override
	public ApiVersion getVersion() {
		return ApiVersion.V1;
	}
}
