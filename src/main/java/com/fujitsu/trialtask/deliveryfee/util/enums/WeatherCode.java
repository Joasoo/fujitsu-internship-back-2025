package com.fujitsu.trialtask.deliveryfee.util.enums;

/** Extra fee codes for different weather conditions. **/
public enum WeatherCode {
    /** Air temperature under -10 degrees Celsius. **/
    ATEF_UNDER_MINUS_TEN,
    /** Air temperature between -10 and 0 degrees Celsius (including). **/
    ATEF_MINUS_TEN_TO_ZERO,
    /** Wind speed between 10m/s and 20m/s (including). **/
    WSEF_TEN_TO_TWENTY,
    /** Wind speed above 20m/s. **/
    WSEF_ABOVE_TWNENTY,
    /** Weather phenomenon includes snow or fleet. **/
    WPEF_SNOW_FLEET,
    /** Weather phenomenon includes rain. **/
    WPEF_RAIN,
    /** Weather phenomenon includes glaze, hail, or thunder. **/
    WPEF_GLAZE_HAIL_THUNDER
}
