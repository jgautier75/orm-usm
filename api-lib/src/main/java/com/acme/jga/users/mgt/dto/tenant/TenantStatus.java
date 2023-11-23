package com.acme.jga.users.mgt.dto.tenant;

public enum TenantStatus {
	ACTIVE("active", 1), INACTIVE("inactive", 2);

	private final String label;
	private final Integer code;

	TenantStatus(String label, Integer code) {
		this.label = label;
		this.code = code;
	}

	public String getLabel() {
		return this.label;
	}

	public Integer getCode() {
		return this.code;
	}
}
