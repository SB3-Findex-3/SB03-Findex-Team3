package com.sprint.findex.domain.indexinfo.exception;

import com.sprint.findex.global.exception.ApiErrorResponse;
import com.sprint.findex.global.exception.custom.IndexInfoNotFoundException;
import com.sprint.findex.global.exception.custom.InvalidIndexInfoCommandException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class IndexInfoExceptionHandler {

    @ExceptionHandler(InvalidIndexInfoCommandException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCommand(
            InvalidIndexInfoCommandException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        "INVALID_INDEX_INFO_COMMAND",
                        ex.getMessage(),
                        HttpStatus.BAD_REQUEST.value()
                ));
    }

    @ExceptionHandler(IndexInfoNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            IndexInfoNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        "INDEX_INFO_NOT_FOUND",
                        ex.getMessage(),
                        HttpStatus.NOT_FOUND.value()
                ));
    }
}
