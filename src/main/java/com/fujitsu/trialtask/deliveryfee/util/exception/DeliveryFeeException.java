package com.fujitsu.trialtask.deliveryfee.util.exception;

/**
 * Thrown when client makes a bad request.
 */
public class DeliveryFeeException extends RuntimeException {

    public DeliveryFeeException(String message) {
        super(message);
    }
}
