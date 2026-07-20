package com.monika.payflow.common.exception;

import com.monika.payflow.common.error.ErrorCode;

public class ConflictException extends RuntimeException {

    private final ErrorCode errorCode;

    public ConflictException(String message) {
        super(message);
        this.errorCode = ErrorCode.EMAIL_ALREADY_EXISTS;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
