package com.acme.jga.search.filtering.expr;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilterComparison {
    public static final String GREATER_THAN = "gt";
    public static final String GREATER_OR_EQUALS = "ge";
    public static final String LOWER_THAN = "lt";
    public static final String LOWER_OR_EQUALS = "le";
    public static final String EEQUALS = "eq";
    public static final String NOT_EQUALS = "ne";
    public static final String LIKE = "lk";
    public static final String SQL_EQUALS = "=";
    public static final String SQL_GREATER_THAN = ">";
    public static final String SQL_GREATER_OR_EQUALS = ">=";
    public static final String SQL_LOWER_THAN = "<";
    public static final String SQL_LOWER_OR_EQUALS = "<=";
    public static final String SQL_NOT_EQUALS = "!=";
    public static final String OPERATOR_AND = "and";
    public static final String OPERATOR_OR = "or";
    public static final String SQL_LIKE = " like ";
}
