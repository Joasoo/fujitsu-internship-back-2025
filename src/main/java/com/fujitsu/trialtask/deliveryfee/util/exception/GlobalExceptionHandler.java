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

    @ExceptionHandler({RuntimeException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ExceptionResponse handleRuntimeException(ServletWebRequest req, RuntimeException e) {
        String URI = req.getRequest().getRequestURI();
        log.error(String.format("URI: %s.\nMessage: %s", URI, e.getMessage()));
        return new ExceptionResponse();
    }

    @ExceptionHandler({})
    @ExceptionHandler({DeliveryFeeException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ExceptionResponse handleDeliveryFeeException(ServletWebRequest req, DeliveryFeeException e) {
        return new ExceptionResponse(e.getMessage());
    }
}
