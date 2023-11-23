package com.acme.jga.users.mgt.dto.api;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class ApiErrorDetail implements Serializable {
	private static final long serialVersionUID = -9004794137547049451L;
	private String code;
	private String field;
	private String message;
}
