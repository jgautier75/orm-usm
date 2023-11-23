package com.acme.jga.users.mgt.dto.phones;

public enum PhoneKind {
	MOBILE("mobile", 0), FIX("fix", 1);

	private final String label;
	private final Integer code;

	PhoneKind(String label, Integer code) {
		this.label = label;
		this.code = code;
	}

	public String getLabel() {
		return label;
	}

	public Integer getCode() {
		return code;
	}

}
