package com.fujitsu.trialtask.deliveryfee.util.exception;

/**
 * Thrown when client makes a bad request (invalid ID, unfit weather conditions).
 */
public class DeliveryFeeException extends RuntimeException {

    public DeliveryFeeException(String message) {
        super(message);
    }
}
