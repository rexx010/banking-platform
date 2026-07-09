package com.bankplatform.shared.exceptions;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // ── Generic
    VALIDATION_FAILED("Validation failed", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("Resource not found", HttpStatus.NOT_FOUND),
    DUPLICATE_RESOURCE("Resource already exists", HttpStatus.CONFLICT),
    INTERNAL_ERROR("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    RATE_LIMIT_EXCEEDED("Too many requests", HttpStatus.TOO_MANY_REQUESTS),
    UNAUTHORIZED("Authentication required", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("Access denied", HttpStatus.FORBIDDEN),

    // ── Auth
    AUTH_INVALID_CREDENTIALS("Invalid email or password", HttpStatus.UNAUTHORIZED),
    AUTH_TOKEN_EXPIRED("Access token has expired", HttpStatus.UNAUTHORIZED),
    AUTH_TOKEN_INVALID("Invalid or malformed token", HttpStatus.UNAUTHORIZED),
    AUTH_REFRESH_INVALID("Refresh token is invalid or expired", HttpStatus.UNAUTHORIZED),
    AUTH_ACCOUNT_LOCKED("Account locked — too many failed attempts", HttpStatus.FORBIDDEN),
    AUTH_OTP_INVALID("OTP is invalid or has expired", HttpStatus.BAD_REQUEST),
    AUTH_PIN_INVALID("Transaction PIN is incorrect", HttpStatus.UNAUTHORIZED),
    AUTH_PIN_NOT_SET("Transaction PIN has not been set", HttpStatus.BAD_REQUEST),

    // ── Identity / BVN
    BVN_NOT_FOUND("BVN not found", HttpStatus.NOT_FOUND),
    BVN_ALREADY_EXISTS("A BVN already exists for this NIN", HttpStatus.CONFLICT),
    KYC_NOT_VERIFIED("KYC verification is required", HttpStatus.FORBIDDEN),
    KYC_ALREADY_SUBMITTED("KYC documents already submitted", HttpStatus.CONFLICT),
    KYC_DOCUMENT_TOO_LARGE("Document exceeds maximum allowed size", HttpStatus.BAD_REQUEST),

    // ── Account
    ACCOUNT_NOT_FOUND("Account not found", HttpStatus.NOT_FOUND),
    ACCOUNT_SUSPENDED("Account is suspended", HttpStatus.FORBIDDEN),
    ACCOUNT_CLOSED("Account is closed", HttpStatus.FORBIDDEN),
    ACCOUNT_DORMANT("Account is dormant", HttpStatus.FORBIDDEN),
    NUBAN_GENERATION_FAILED("Failed to generate unique NUBAN", HttpStatus.INTERNAL_SERVER_ERROR),

    // ── Transfer
    TRANSFER_NOT_FOUND("Transfer not found", HttpStatus.NOT_FOUND),
    TRANSFER_INSUFFICIENT_FUNDS("Insufficient funds", HttpStatus.BAD_REQUEST),
    TRANSFER_SAME_ACCOUNT("Source and destination cannot be the same", HttpStatus.BAD_REQUEST),
    TRANSFER_LIMIT_EXCEEDED("Transfer amount exceeds daily limit", HttpStatus.BAD_REQUEST),
    TRANSFER_DUPLICATE("Duplicate transfer request", HttpStatus.CONFLICT),
    TRANSFER_FAILED("Transfer could not be completed", HttpStatus.UNPROCESSABLE_ENTITY),

    // ── Card
    CARD_NOT_FOUND("Card not found", HttpStatus.NOT_FOUND),
    CARD_EXPIRED("Card has expired", HttpStatus.BAD_REQUEST),
    CARD_BLOCKED("Card is blocked", HttpStatus.FORBIDDEN),
    CARD_INVALID_CVV("Invalid CVV", HttpStatus.BAD_REQUEST),
    CARD_PIN_INVALID("Invalid card PIN", HttpStatus.UNAUTHORIZED),
    CARD_SPENDING_LIMIT("Transaction exceeds card spending limit", HttpStatus.BAD_REQUEST),
    CARD_ALREADY_ISSUED("A card already exists for this account", HttpStatus.CONFLICT);

    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ErrorCode(String defaultMessage, HttpStatus httpStatus){
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }

    public String getDefaultMessage() {return defaultMessage;}
    public HttpStatus getHttpStatus(){return httpStatus;}
    public int getHttpStatusCode(){return httpStatus.value();}
}