package com.fujitsu.trialtask.deliveryfee.util.exception;

public class ExceptionResponse {
    private static final String GENERIC_MESSAGE = "An unexpected error has occurred.";
    private final String message;

    public ExceptionResponse(String message) {
        this.message = message;
    }

    public ExceptionResponse() {
        this(GENERIC_MESSAGE);
    }
}
