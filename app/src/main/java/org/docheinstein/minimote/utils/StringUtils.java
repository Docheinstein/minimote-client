package org.docheinstein.minimote.utils;

public class StringUtils {
    public static boolean isValid(String str) {
        return str != null && !str.isEmpty();
    }

    public static String firstValid(String... strs) {
        for (String str : strs)
            if (isValid(str))
                return str;
        return null;
    }
}

