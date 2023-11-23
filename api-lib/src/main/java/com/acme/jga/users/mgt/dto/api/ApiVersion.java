package com.acme.jga.users.mgt.dto.api;

public enum ApiVersion {

	V1("v1");

	private final String version;

	ApiVersion(String aVersion) {
		this.version = aVersion;
	}

	public String getVersion() {
		return version;
	}

}
