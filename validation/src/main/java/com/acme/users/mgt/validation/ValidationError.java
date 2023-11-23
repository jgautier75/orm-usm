package com.acme.users.mgt.validation;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class ValidationError implements Serializable {
    private String fieldName;
    private Object fieldValue;
    private String validationRule;
    private String message;
}
