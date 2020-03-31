package org.docheinstein.minimote.utils;

public class IntUtils {
    public static Integer parseString(String str) {
        return parseString(str, null);
    }

    public static Integer parseString(String str, Integer defaultValue) {
        Integer val;
        try {
            val = Integer.valueOf(str);
        } catch (NumberFormatException nfe) {
            val = defaultValue;
        }

        return val;
    }
}
