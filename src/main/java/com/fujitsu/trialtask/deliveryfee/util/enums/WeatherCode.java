package com.fujitsu.trialtask.deliveryfee.util.enums;

/** Codes for different weather conditions. **/
public enum WeatherCode {
    /** Air temperature under -10 degrees Celsius. **/
    AT_UNDER_MINUS_TEN,
    /** Air temperature between -10 to 0 degrees Celsius (including). **/
    AT_MINUS_TEN_TO_ZERO,
    /** Wind speed between 10m/s to 20m/s (including). **/
    WS_TEN_TO_TWENTY,
    /** Wind speed above 20m/s. **/
    WS_ABOVE_TWENTY,
    /** Weather phenomenon includes snow or fleet. **/
    WP_SNOW_FLEET,
    /** Weather phenomenon includes rain. **/
    WP_RAIN,
    /** Weather phenomenon includes glaze, hail, or thunder. **/
    WP_GLAZE_HAIL_THUNDER
}
