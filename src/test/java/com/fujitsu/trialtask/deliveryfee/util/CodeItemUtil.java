package com.fujitsu.trialtask.deliveryfee.util;

import com.fujitsu.trialtask.deliveryfee.entity.CodeItem;


public class CodeItemUtil {
    private CodeItemUtil() {
    }

    public static final CodeItem AT_UNDER_MINUS_TEN = getCodeItem("AT_UNDER_MINUS_TEN");
    public static final CodeItem AT_MINUS_TEN_TO_ZERO = getCodeItem("AT_MINUS_TEN_TO_ZERO");
    public static final CodeItem WS_TEN_TO_TWENTY = getCodeItem("WS_TEN_TO_TWENTY");
    public static final CodeItem WS_ABOVE_TWENTY = getCodeItem("WS_ABOVE_TWENTY");
    public static final CodeItem WP_SNOW_SLEET = getCodeItem("WP_SNOW_SLEET");
    public static final CodeItem WP_RAIN = getCodeItem("WP_RAIN");
    public static final CodeItem WP_GLAZE_HAIL_THUNDER = getCodeItem("WP_GLAZE_HAIL_THUNDER");

    private static CodeItem getCodeItem(String code) {
        String codeClass = code.split("_", 1)[0];
        return new CodeItem(code, codeClass);
    }
}
