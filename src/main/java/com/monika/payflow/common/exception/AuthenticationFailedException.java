package com.monika.payflow.common.exception;

import com.monika.payflow.common.error.ErrorCode;

public class AuthenticationFailedException extends RuntimeException {

    private final ErrorCode errorCode;

    public AuthenticationFailedException(String message) {
        super(message);
        this.errorCode = ErrorCode.INVALID_CREDENTIALS;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
