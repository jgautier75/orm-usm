package com.acme.jga.users.mgt.dto.pagination;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class WhereClause {
	private String expression;
	private WhereOperator operator;
	private String paramName;
	private Object paramValue;
	private List<String> paramNames;
	private List<Object> paramValues;
}
