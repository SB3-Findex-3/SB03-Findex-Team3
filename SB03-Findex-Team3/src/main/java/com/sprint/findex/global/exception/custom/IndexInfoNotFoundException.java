package com.sprint.findex.global.exception.custom;

public class IndexInfoNotFoundException extends RuntimeException {
    public IndexInfoNotFoundException(String message) {
        super(message);
    }
}
