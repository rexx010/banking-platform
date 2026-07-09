package com.bankplatform.shared.exceptions;

public class BankException extends RuntimeException{
    private final ErrorCode errorCode;

    public BankException(ErrorCode errorCode){
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public BankException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BankException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
