package com.fujitsu.trialtask.deliveryfee.util.exception;


public class DeliveryFeeException extends RuntimeException {
    public enum Reason {
        INVALID_CITY_ID,
        INVALID_VEHICLE_ID,
        UNFIT_WEATHER_CONDITIONS,
        BASE_FEE_DOES_NOT_EXIST,
    }

    private final Reason reason;

    public DeliveryFeeException(String message, Reason reason) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
