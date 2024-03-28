package com.fujitsu.trialtask.deliveryfee.util.exception;


public class WeatherDataException extends RuntimeException {
    private final Integer WMOcode;

    public WeatherDataException(String message, Integer WMOcode) {
        super(message);
        this.WMOcode = WMOcode;
    }

    public Integer getWMOcode() {
        return WMOcode;
    }
}
