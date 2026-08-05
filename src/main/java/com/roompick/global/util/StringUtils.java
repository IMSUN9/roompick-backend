package com.roompick.global.util;

public final class StringUtils {

    private StringUtils() {
    }

    public static String trimToEmpty(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
