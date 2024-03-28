package com.fujitsu.trialtask.deliveryfee.util.exception;


public class CodeItemException extends RuntimeException {
    private final String code;

    public CodeItemException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
