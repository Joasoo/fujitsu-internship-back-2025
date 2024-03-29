package com.fujitsu.trialtask.deliveryfee.util.exception;

/**
 * Thrown when an exception occurs while requesting new weather data.
 */
public class WeatherRequestException extends RuntimeException {

    public WeatherRequestException(String message) {
        super(message);
    }

    public WeatherRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
