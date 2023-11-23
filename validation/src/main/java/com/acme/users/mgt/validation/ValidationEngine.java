package com.acme.users.mgt.validation;

public interface ValidationEngine<T> {
    ValidationResult validate(T object);
}
