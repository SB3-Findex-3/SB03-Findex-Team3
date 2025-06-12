package com.sprint.findex.global.exception.custom;

public class InvalidSortFieldException extends RuntimeException {

    public InvalidSortFieldException(String message) {
        super(message);
    }

    public InvalidSortFieldException(String message, Throwable cause) {
        super(message, cause);
    }
}
