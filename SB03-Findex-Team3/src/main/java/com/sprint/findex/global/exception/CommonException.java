package com.sprint.findex.global.exception;

public class CommonException extends RuntimeException {

    private final Errors error;

    public CommonException(Errors error) {
        super(error.getMessage());
        this.error = error;
    }

    public CommonException(Errors error, Throwable cause) {
        super(error.getMessage(), cause);
        this.error = error;
    }

    public Errors getError() {
        return this.error;
    }
}