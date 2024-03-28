package com.fujitsu.trialtask.deliveryfee.util.exception;


public class WeatherCodeItemException extends DeliveryFeeException {
    private final String code;

    public WeatherCodeItemException(String message, Reason reason, String code) {
        super(message, reason);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
