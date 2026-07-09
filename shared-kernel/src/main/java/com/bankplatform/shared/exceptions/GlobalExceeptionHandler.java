package com.bankplatform.shared.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceeptionHandler {
    
    @ExceptionHandler(BankException.class)
    public ResponseEntity<ErrorResponse> handleBankException(BankException ex, HttpServletRequest request){
        ErrorCode code = ex.getErrorCode();
        log.warn("Business error [{}] on {}: {}",
                code.name(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity
                .status(code.getHttpStatus())
                .body(ErrorResponse.of(
                        code, ex.getMessage(),
                        request.getRequestURI(),
                        MDC.get("traceId")
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request){
        List<Map<String, String>> fieldErrors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String field = error instanceof FieldError fe
                            ? fe.getField()
                            : error.getObjectName();
                    String message = error.getDefaultMessage();
                    return Map.of("field", field, "message", message);
                })
                .toList();

        log.warn("Validation failed on {}: {} errors",
                request.getRequestURI(), fieldErrors.size());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.ofValidation(
                        request.getRequestURI(),
                        MDC.get("traceId"),
                        fieldErrors
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request){
        log.error("Unexpected error on {}: {}",
                request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        ErrorCode.INTERNAL_ERROR,
                        ErrorCode.INTERNAL_ERROR.getDefaultMessage(),
                        request.getRequestURI(),
                        MDC.get("traceId")
                ));
    }
}
