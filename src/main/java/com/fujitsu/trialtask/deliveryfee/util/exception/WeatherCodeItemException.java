package com.fujitsu.trialtask.deliveryfee.util.exception;

import com.fujitsu.trialtask.deliveryfee.util.enums.WeatherCode;

public class WeatherCodeItemException extends DeliveryFeeException {

    private final WeatherCode code;
    public WeatherCodeItemException(String message, Reason reason, WeatherCode code) {
        super(message, reason);
        this.code = code;
    }

    public WeatherCode getCode() {
        return code;
    }

}
