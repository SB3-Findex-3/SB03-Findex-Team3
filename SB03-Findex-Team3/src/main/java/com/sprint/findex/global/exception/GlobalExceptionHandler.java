package com.sprint.findex.global.exception;

import com.sprint.findex.dto.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<ErrorResponse> handleCommonException(CommonException e) {
        Errors error = e.getError();
        return ResponseEntity
            .status(error.getStatus())
            .body(ErrorResponse.of(error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        Errors error = Errors.UNKNOWN_ERROR;
        return ResponseEntity
            .status(error.getStatus())
            .body(ErrorResponse.of(error));
    }
}