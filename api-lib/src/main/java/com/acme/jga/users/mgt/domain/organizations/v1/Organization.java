package com.acme.jga.users.mgt.domain.organizations.v1;

import java.io.Serializable;

import org.apache.commons.lang3.builder.DiffBuilder;
import org.apache.commons.lang3.builder.DiffResult;
import org.apache.commons.lang3.builder.Diffable;
import org.apache.commons.lang3.builder.ToStringStyle;

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
public class Organization implements IVersioned, Serializable, Diffable<Organization> {
	private static final long serialVersionUID = -627071470081034100L;
	private Long tenantId;
	private Long id;
	private String uid;
	private OrganizationCommons commons;
	private Sector sector;

	@Override
	public ApiVersion getVersion() {
		return ApiVersion.V1;
	}

	@Override
	public DiffResult<Organization> diff(Organization obj) {
		return new DiffBuilder<Organization>(this, obj, ToStringStyle.SHORT_PREFIX_STYLE)
       .append("commons", this.commons, obj.commons)       
       .build();
	}
}
