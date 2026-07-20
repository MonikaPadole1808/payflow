package com.monika.payflow.common.exception;

import com.monika.payflow.common.error.ErrorCode;

public class ResourceNotFoundException extends RuntimeException {

    private final ErrorCode errorCode;

    public ResourceNotFoundException(String message) {
        super(message);
        this.errorCode = ErrorCode.RESOURCE_NOT_FOUND;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
