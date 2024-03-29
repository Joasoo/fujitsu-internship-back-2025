package com.fujitsu.trialtask.deliveryfee.util.exception;

/**
 * Thrown when a code is requested from database that does not exist there.
 */
public class CodeItemException extends RuntimeException {

    public CodeItemException(String code) {
        super("CodeItem does not exist for given code: " + code);
    }
}
