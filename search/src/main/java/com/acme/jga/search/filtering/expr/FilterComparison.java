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
}
