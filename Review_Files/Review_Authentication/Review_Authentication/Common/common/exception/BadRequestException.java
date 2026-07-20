package com.monika.payflow.common.exception;

import com.monika.payflow.common.error.ErrorCode;

public class BadRequestException extends RuntimeException {

    private final ErrorCode errorCode;

    public BadRequestException(String message) {
        super(message);
        this.errorCode = ErrorCode.BAD_REQUEST;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
