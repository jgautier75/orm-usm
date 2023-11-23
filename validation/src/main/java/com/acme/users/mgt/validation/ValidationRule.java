package com.acme.users.mgt.validation;

public enum ValidationRule {
    NOT_EMPTY,
    NOT_NULL,
    LIST_NOT_NULL_NOT_EMPTY,
    EMAIL,
    PAYLOAD,
    COUNTRY_ISO,
    LENGTH;
}
