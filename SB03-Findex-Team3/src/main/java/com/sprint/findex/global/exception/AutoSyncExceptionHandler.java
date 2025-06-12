package com.sprint.findex.global.exception;

import com.sprint.findex.global.exception.custom.AutoSyncConfigCreationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AutoSyncExceptionHandler {

    @ExceptionHandler(AutoSyncConfigCreationException.class)
    public ResponseEntity<ApiErrorResponse> handleAutoSyncCreation(AutoSyncConfigCreationException ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of("AUTO_SYNC_CONFIG_ERROR", ex.getMessage(), 500));
    }
}