package com.acme.users.mgt.validation.organizations;

import org.springframework.stereotype.Component;
import com.acme.users.mgt.dto.port.organizations.v1.OrganizationDto;
import com.acme.users.mgt.validation.ValidationEngine;
import com.acme.users.mgt.validation.ValidationResult;
import com.acme.users.mgt.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrganizationsValidationEngine implements ValidationEngine<OrganizationDto> {
        private final ValidationUtils validationUtils;

        @Override
        public ValidationResult validate(OrganizationDto organizationDto) {
                ValidationResult validationResult = ValidationResult.builder().success(true).build();
                // Validate payload
                if (validationUtils.validateNotNull(validationResult, "payload", organizationDto)
                                && validationUtils.validateNotNull(validationResult, "commons",
                                                organizationDto.getCommons())) {
                        validationUtils.validateNotNullNonEmpty(validationResult, "commons.code",
                                        organizationDto.getCommons().getCode());
                        if (validationUtils.validateNotNullNonEmpty(validationResult, "commons.country",
                                        organizationDto.getCommons().getCountry())) {
                                validationUtils.validateCountry(validationResult, "commons.country",
                                                organizationDto.getCommons().getCountry());
                        }
                        validationUtils.validateNotNullNonEmpty(validationResult, "commons.label",
                                        organizationDto.getCommons().getLabel());
                        validationUtils.validateNotNull(validationResult, "commons.kind",
                                        organizationDto.getCommons().getKind());
                        validationUtils.validateNotNull(validationResult, "commons.status",
                                        organizationDto.getCommons().getStatus());
                }
                return validationResult;
        }

}
