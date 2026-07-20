package com.monika.payflow.common.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    RESOURCE_NOT_FOUND("PF-404-001", "Requested resource was not found", HttpStatus.NOT_FOUND),
    BAD_REQUEST("PF-400-001", "Invalid request", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED("PF-400-002", "Validation failed", HttpStatus.BAD_REQUEST),
    MALFORMED_REQUEST("PF-400-003", "Malformed request body", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("PF-401-001", "Authentication is required", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS("PF-401-002", "Invalid email or password", HttpStatus.UNAUTHORIZED),
    EMAIL_ALREADY_EXISTS("PF-409-001", "Email already exists", HttpStatus.CONFLICT),
    INVALID_REFRESH_TOKEN("PF-401-003", "Refresh token is invalid or expired", HttpStatus.UNAUTHORIZED),
    INTERNAL_SERVER_ERROR("PF-500-001", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String defaultMessage, HttpStatus httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }

    public String code() {
        return code;
    }

    public String defaultMessage() {
        return defaultMessage;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }
}
