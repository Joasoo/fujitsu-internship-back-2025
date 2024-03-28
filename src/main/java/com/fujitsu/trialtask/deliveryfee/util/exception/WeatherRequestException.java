package com.fujitsu.trialtask.deliveryfee.util.exception;

public class WeatherRequestException extends RuntimeException {

    public WeatherRequestException(String message) {
        super(message);
    }

    public WeatherRequestException(String message, Throwable cause) {
        super(message, cause);
    }

}
