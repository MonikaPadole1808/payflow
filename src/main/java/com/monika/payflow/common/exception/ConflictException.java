package com.monika.payflow.common.exception;

import com.monika.payflow.common.error.ErrorCode;

public class ConflictException extends RuntimeException {

    private final ErrorCode errorCode;

    public ConflictException(String message) {
        this(message, ErrorCode.EMAIL_ALREADY_EXISTS);
    }

    public ConflictException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
