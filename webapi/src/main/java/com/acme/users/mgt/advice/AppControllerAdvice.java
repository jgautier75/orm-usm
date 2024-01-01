package com.acme.users.mgt.advice;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import com.acme.jga.users.mgt.dto.api.ApiError;
import com.acme.jga.users.mgt.dto.api.ApiErrorDetail;
import com.acme.jga.users.mgt.dto.api.ErrorKind;
import com.acme.jga.users.mgt.exceptions.FunctionalErrorsTypes;
import com.acme.jga.users.mgt.exceptions.FunctionalException;
import com.acme.users.mgt.config.AppGenericConfig;
import com.acme.users.mgt.logging.services.api.ILogService;
import com.acme.users.mgt.logging.utils.LogHttpUtils;
import com.acme.users.mgt.validation.ValidationException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ControllerAdvice
public class AppControllerAdvice {
    private final ILogService logService;
    private final AppGenericConfig appGenericConfig;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleInternal(Exception exception) throws IOException {
        UUID idError = UUID.randomUUID();
        String stack = null;
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            exception.printStackTrace(pw);
            stack = sw.toString();
        }
        final var apiError = ApiError.builder()
                .kind(ErrorKind.TECHNICAL)
                .code("INTERNAL_SERVER_ERROR")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(exception.getMessage())
                .debugMessage(stack)
                .errorUid(idError.toString())
                .build();
        try {
            logService.errorS(this.getClass().getName() + "-handleInternal", "Process error to %s - %s - %s",
                    new Object[] { appGenericConfig.getErrorPath(), appGenericConfig.getModuleName(),
                            idError.toString() });
            LogHttpUtils.dumpToFile(logService, appGenericConfig.getErrorPath(), appGenericConfig.getModuleName(),
                    idError.toString(), stack);
        } catch (Exception e) {
            logService.error(this.getClass().getName() + "-handleInternal", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(apiError);

    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidationException(Exception ex, WebRequest request) {
        List<ApiErrorDetail> errorDetailList = new ArrayList<>();
        final ApiError apiError = ApiError.builder()
                .kind(ErrorKind.FUNCTIONAL)
                .status(HttpStatus.BAD_REQUEST.value())
                .code(HttpStatus.BAD_REQUEST.name())
                .details(errorDetailList)
                .message("ArgumentNotValid").build();
        if (((ValidationException) ex).getValidationErrors() != null) {
            ((ValidationException) ex).getValidationErrors().forEach(validationError -> {
                final ApiErrorDetail apiErrorDetail = ApiErrorDetail.builder()
                        .code(validationError.getValidationRule())
                        .field(validationError.getFieldName())
                        .message(validationError.getMessage())
                        .build();
                errorDetailList.add(apiErrorDetail);
            });
        }
        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(FunctionalException.class)
    public ResponseEntity<Object> handleFunctionalException(Exception ex, WebRequest request) {
        // Defaults to 400-BAD_REQUEST
        Integer targetStatus = HttpStatus.BAD_REQUEST.value();

        if (isConflict((FunctionalException) ex)) {
            // 409-CONFLICT
            targetStatus = HttpStatus.CONFLICT.value();
        } else if (isNotFound(((FunctionalException) ex))) {
            // 404-NOT_FOUND
            targetStatus = HttpStatus.NOT_FOUND.value();
        }
        ApiError apiError = ApiError.builder()
                .code(((FunctionalException) ex).getCode())
                .message(((FunctionalException) ex).getMessage())
                .status(targetStatus)
                .build();
        return ResponseEntity.status(targetStatus).body(apiError);
    }

    private boolean isConflict(FunctionalException exception) {
        return FunctionalErrorsTypes.TENANT_ORG_EXPECTED.name().equals(exception.getCode())
                || FunctionalErrorsTypes.TENANT_CODE_ALREADY_USED.name().equals(exception.getCode())
                || FunctionalErrorsTypes.ORG_CODE_ALREADY_USED.name().equals(exception.getCode())
                || FunctionalErrorsTypes.SECTOR_CODE_ALREADY_USED.name().equals(exception.getCode())
                || FunctionalErrorsTypes.USER_EMAIL_ALREADY_USED.name().equals(exception.getCode())
                || FunctionalErrorsTypes.USER_LOGIN_ALREADY_USED.name().equals(exception.getCode());
    }

    private boolean isNotFound(FunctionalException exception) {
        return FunctionalErrorsTypes.TENANT_CODE_ALREADY_USED.name().equals(exception.getCode())
                || FunctionalErrorsTypes.ORG_NOT_FOUND.name().equals(exception.getCode())
                || FunctionalErrorsTypes.USER_NOT_FOUND.name().equals(exception.getCode())
                || FunctionalErrorsTypes.SECTOR_NOT_FOUND.name().equals(exception.getCode());
    }

}
