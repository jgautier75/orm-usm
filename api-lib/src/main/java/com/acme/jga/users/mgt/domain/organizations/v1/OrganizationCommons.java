package com.acme.jga.users.mgt.domain.organizations.v1;

import java.io.Serializable;

import org.apache.commons.lang3.builder.DiffBuilder;
import org.apache.commons.lang3.builder.DiffResult;
import org.apache.commons.lang3.builder.Diffable;
import org.apache.commons.lang3.builder.ToStringStyle;

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
public class OrganizationCommons implements IVersioned, Serializable, Diffable<OrganizationCommons> {
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

	@Override
	public DiffResult<OrganizationCommons> diff(OrganizationCommons obj) {
		return new DiffBuilder<OrganizationCommons>(this, obj, ToStringStyle.SHORT_PREFIX_STYLE)
       .append("code", this.code, obj.code)
       .append("label", this.label, obj.label)
       .append("kind", this.kind, obj.kind)
	   .append("country", this.country, obj.country)
	   .append("status", this.status, obj.status)
       .build();
	}
}
