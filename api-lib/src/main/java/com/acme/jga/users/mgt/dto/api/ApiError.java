package com.acme.jga.users.mgt.dto.api;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class ApiError implements Serializable {
	private static final long serialVersionUID = 7220109783895723031L;
	private ErrorKind kind;
	private String code;
	private String message;
	private String debugMessage;
	private Integer status;
	private List<ApiErrorDetail> details;
}
