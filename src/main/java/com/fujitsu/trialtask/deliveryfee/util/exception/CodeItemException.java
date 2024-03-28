package com.fujitsu.trialtask.deliveryfee.util.exception;


public class CodeItemException extends RuntimeException {

    public CodeItemException(String code) {
        super("CodeItem does not exist for given code: " + code);
    }
}
