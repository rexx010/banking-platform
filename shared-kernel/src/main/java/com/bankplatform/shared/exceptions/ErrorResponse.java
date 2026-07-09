package com.bankplatform.shared.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int                       status,
        String                    error,
        String                    message,
        String                    path,
        Instant                   timestamp,
        String                    traceId,
        List<Map<String, String>> errors
) {

    public static ErrorResponse of(
            ErrorCode code,
            String message,
            String path,
            String traceId
    ){
        return new ErrorResponse(
                code.getHttpStatusCode(),
                code.name(),
                message,
                path,
                Instant.now(),
                traceId,
                null
        );
    }

    public static ErrorResponse ofValidation(
            String path,
            String traceId,
            List<Map<String, String>> fieldErrors
    ){
        return new ErrorResponse(
                400,
                ErrorCode.VALIDATION_FAILED.name(),
                ErrorCode.VALIDATION_FAILED.getDefaultMessage(),
                path,
                Instant.now(),
                traceId,
                fieldErrors
        );
    }
}
