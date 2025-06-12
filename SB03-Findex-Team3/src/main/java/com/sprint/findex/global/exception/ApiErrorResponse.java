package com.sprint.findex.global.exception;

import java.time.LocalDateTime;

public record ApiErrorResponse(
    String errorCode,
    String message,
    int status,
    LocalDateTime timestamp
) {
    public static ApiErrorResponse of(String errorCode, String message, int status) {
        return new ApiErrorResponse(
            errorCode,
            message,
            status,
            LocalDateTime.now()
        );
    }
}
