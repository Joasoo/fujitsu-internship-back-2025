package com.fujitsu.trialtask.deliveryfee.util.exception;

/**
 * Response to client when an exception is thrown.
 */
public class ExceptionResponse {
    private final String message;

    public ExceptionResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
