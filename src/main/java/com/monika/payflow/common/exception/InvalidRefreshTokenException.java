package com.monika.payflow.common.exception;

import com.monika.payflow.common.error.ErrorCode;

public class InvalidRefreshTokenException extends RuntimeException {

    private final ErrorCode errorCode;

    public InvalidRefreshTokenException() {
        super(ErrorCode.INVALID_REFRESH_TOKEN.defaultMessage());
        this.errorCode = ErrorCode.INVALID_REFRESH_TOKEN;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
