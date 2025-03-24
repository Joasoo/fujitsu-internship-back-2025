package com.fujitsu.trialtask.deliveryfee.util.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;

@Slf4j
@ControllerAdvice
public final class GlobalExceptionHandler {
    private static final String GENERIC_MESSAGE = "An unexpected error has occurred";

    @ExceptionHandler({RuntimeException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ExceptionResponse handleRuntimeException(ServletWebRequest req, RuntimeException e) {
        String URI = req.getRequest().getRequestURI();
        log.error("URI: " + URI, e);
        return new ExceptionResponse(GENERIC_MESSAGE);
    }

    @ExceptionHandler({WeatherDataException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ExceptionResponse handleWeatherDataException(WeatherDataException e) {
        log.error(String.format("err: %s. WMO: %d", e.getMessage(), e.getWMOcode()), e);
        return new ExceptionResponse(e.getMessage());
    }

    @ExceptionHandler({DeliveryFeeException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ExceptionResponse handleDeliveryFeeException(DeliveryFeeException e) {
        return new ExceptionResponse(e.getMessage());
    }

    @ExceptionHandler({WeatherRequestException.class})
    public void handleWeatherRequestException(WeatherRequestException e) {
        if (e.getCause() == null) {
            log.error(e.getMessage(), e);
        } else {
            log.error(e.getMessage(), e, e.getCause());
        }
    }
}
