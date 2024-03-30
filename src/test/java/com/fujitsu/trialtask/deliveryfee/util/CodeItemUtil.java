package com.fujitsu.trialtask.deliveryfee.util;

import com.fujitsu.trialtask.deliveryfee.entity.CodeItem;
import com.fujitsu.trialtask.deliveryfee.util.enums.WeatherCode;

public class CodeItemUtil {
    private CodeItemUtil() {}
    public static final CodeItem AT_UNDER_MINUS_TEN = getCodeItem(WeatherCode.AT_UNDER_MINUS_TEN.name());
    public static final CodeItem AT_MINUS_TEN_TO_ZERO = getCodeItem(WeatherCode.AT_MINUS_TEN_TO_ZERO.name());
    public static final CodeItem WS_TEN_TO_TWENTY = getCodeItem(WeatherCode.WS_TEN_TO_TWENTY.name());
    public static final CodeItem WS_ABOVE_TWENTY = getCodeItem(WeatherCode.WS_ABOVE_TWENTY.name());
    public static final CodeItem WP_SNOW_SLEET = getCodeItem(WeatherCode.WP_SNOW_SLEET.name());
    public static final CodeItem WP_RAIN = getCodeItem(WeatherCode.WP_RAIN.name());
    public static final CodeItem WP_GLAZE_HAIL_THUNDER = getCodeItem(WeatherCode.WP_GLAZE_HAIL_THUNDER.name());

    private static CodeItem getCodeItem(String code) {
        String codeClass = code.split("_", 1)[0];
        return new CodeItem(code, codeClass);
    }
}
