package com.acme.users.mgt.validation.tenants;

import org.springframework.stereotype.Component;
import com.acme.users.mgt.dto.port.tenants.v1.TenantDto;
import com.acme.users.mgt.validation.ValidationEngine;
import com.acme.users.mgt.validation.ValidationResult;
import com.acme.users.mgt.validation.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TenantsValidationEngine implements ValidationEngine<TenantDto> {
    private final ValidationUtils validationUtils;

    @Override
    public ValidationResult validate(TenantDto tenantDto) {
        ValidationResult validationResult = ValidationResult.builder().success(true).build();
        if (validationUtils.validateNotNull(validationResult, "payload", tenantDto)) {
            validationUtils.validateNotNullNonEmpty(validationResult, "code", tenantDto.getCode());
            validationUtils.validateNotNullNonEmpty(validationResult, "label", tenantDto.getLabel());
        }
        return validationResult;
    }

}
