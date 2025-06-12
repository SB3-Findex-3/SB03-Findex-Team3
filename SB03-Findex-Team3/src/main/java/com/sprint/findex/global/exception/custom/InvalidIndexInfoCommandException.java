package com.sprint.findex.global.exception.custom;

public class InvalidIndexInfoCommandException extends RuntimeException {

    public InvalidIndexInfoCommandException(String message) {
        super(message);
    }

    public InvalidIndexInfoCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
