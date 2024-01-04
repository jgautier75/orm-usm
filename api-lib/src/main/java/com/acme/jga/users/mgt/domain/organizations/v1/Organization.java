package com.acme.jga.users.mgt.domain.organizations.v1;

import java.io.Serializable;
import java.util.List;

import com.acme.jga.users.mgt.domain.addresses.v1.Address;
import com.acme.jga.users.mgt.domain.delegations.v1.Delegation;
import com.acme.jga.users.mgt.domain.phones.v1.Phone;
import com.acme.jga.users.mgt.domain.sectors.v1.Sector;
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
public class Organization implements IVersioned, Serializable {
	private static final long serialVersionUID = -627071470081034100L;
	private Long tenantId;
	private Long id;
	private String uid;
	private OrganizationCommons commons;
	private List<Phone> phones;
	private List<Address> addresses;
	private List<Delegation> delegations;
	private Sector sector;

	@Override
	public ApiVersion getVersion() {
		return ApiVersion.V1;
	}
}
