package com.acme.users.mgt.validation.sectors;

import org.springframework.stereotype.Component;

import com.acme.users.mgt.dto.port.sectors.v1.SectorDto;
import com.acme.users.mgt.validation.ValidationEngine;
import com.acme.users.mgt.validation.ValidationResult;
import com.acme.users.mgt.validation.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SectorsValidationEngine implements ValidationEngine<SectorDto> {
    private final ValidationUtils validationUtils;

    @Override
    public ValidationResult validate(SectorDto sectorDto) {
        ValidationResult validationResult = ValidationResult.builder().success(true).build();
        if (validationUtils.validateNotNull(validationResult, "payload", sectorDto)) {
            validationUtils.validateNotNullNonEmpty(validationResult, "code", sectorDto.getCode());
            validationUtils.validateNotNullNonEmpty(validationResult, "label", sectorDto.getLabel());
            validationUtils.validateNotNullNonEmpty(validationResult, "parentUid", sectorDto.getParentUid());
        }
        return validationResult;
    }

}
