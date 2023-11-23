package com.acme.users.mgt.validation.users;

import org.springframework.stereotype.Component;
import com.acme.users.mgt.dto.port.users.v1.UserCommonsDto;
import com.acme.users.mgt.dto.port.users.v1.UserDto;
import com.acme.users.mgt.validation.ValidationEngine;
import com.acme.users.mgt.validation.ValidationError;
import com.acme.users.mgt.validation.ValidationResult;
import com.acme.users.mgt.validation.ValidationRule;
import com.acme.users.mgt.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UsersValidationEngine implements ValidationEngine<UserDto> {
        private final ValidationUtils validationUtils;

        @Override
        public ValidationResult validate(UserDto userDto) {
                ValidationResult validationResult = ValidationResult.builder().success(true).build();

                // Validate payload not null
                validationUtils.validateNotNull(validationResult, "payload", userDto);

                if (userDto != null) {
                        // Validate commons not null
                        if (validationUtils.validateNotNull(validationResult, "commons", userDto.getCommons())) {
                                validateCommmons(validationResult, userDto.getCommons());
                        }

                        // Validate credentials
                        validationUtils.validateNotNull(validationResult, "credentials", userDto.getCredentials());
                        if (userDto.getCredentials() != null) {
                                // Validate login
                                validationUtils.validateNotNullNonEmpty(validationResult, "credentials.login",
                                                userDto.getCredentials().getLogin());
                                if (userDto.getCredentials().getLogin() != null) {
                                        validationUtils.validateTextLength(validationResult, "credentials.login",
                                                        userDto.getCredentials().getLogin(), 1, 50);
                                }
                                // Validate email
                                if (validationUtils.validateNotNullNonEmpty(validationResult, "credentials.email",
                                                userDto.getCredentials().getEmail())) {
                                        validationUtils.validateTextLength(validationResult, "credentials.email",
                                                        userDto.getCredentials().getLogin(), 1, 50);
                                        if (!validationUtils.isValidEmail(userDto.getCredentials().getEmail())) {
                                                validationResult.setSuccess(false);
                                                validationResult.addError(ValidationError.builder()
                                                                .fieldName("credentials.email")
                                                                .fieldValue(userDto.getCredentials().getEmail())
                                                                .message(validationUtils.buildInvalidEmail(
                                                                                "credentials.email",
                                                                                ValidationUtils.EMAIL_REGEX.pattern()))
                                                                .validationRule(ValidationRule.EMAIL.name())
                                                                .build());
                                        }
                                }
                        }

                        // Validate status
                        validationUtils.validateNotNull(validationResult, "status", userDto.getStatus());
                }

                return validationResult;
        }

        public void validateCommmons(ValidationResult validationResult, UserCommonsDto commonsDto) {
                // Validate firstName
                validationUtils.validateNotNullNonEmpty(validationResult, "commons.firstName",
                                commonsDto.getFirstName());
                if (commonsDto.getFirstName() != null) {
                        validationUtils.validateTextLength(validationResult, "commons.firstName",
                                        commonsDto.getFirstName(), 1, 50);
                }

                // Validate lastName
                validationUtils.validateNotNullNonEmpty(validationResult, "commons.lastName",
                                commonsDto.getLastName());
                if (commonsDto.getLastName() != null) {
                        validationUtils.validateTextLength(validationResult, "commons.lastName",
                                        commonsDto.getLastName(), 1, 50);
                }

                // Validate middleName
                validationUtils.validateNotNullNonEmpty(validationResult, "commons.middleName",
                                commonsDto.getMiddleName());
                if (commonsDto.getMiddleName() != null) {
                        validationUtils.validateTextLength(validationResult, "commons.middleName",
                                        commonsDto.getMiddleName(), 1, 5);
                }
        }

}
