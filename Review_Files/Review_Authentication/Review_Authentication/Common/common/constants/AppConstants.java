package com.monika.payflow.common.constants;

public final class AppConstants {

    public static final String API_BASE_PATH = "/api/v1";
    public static final String DEFAULT_SUCCESS_MESSAGE = "Request completed successfully";
    public static final String VALIDATION_FAILED_MESSAGE = "Validation failed";
    public static final String INTERNAL_SERVER_ERROR_MESSAGE = "An unexpected error occurred";

    private AppConstants() {
        throw new UnsupportedOperationException("AppConstants cannot be instantiated");
    }
}
